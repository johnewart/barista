package net.johnewart.barista.data.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.VersionConstraint;
import net.johnewart.barista.core.cookbook.CookbookFilter;
import net.johnewart.barista.data.CookbookDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.*;

public class RedisCookbookDAO implements CookbookDAO {
    private final JedisPool jedisPool;
    private final ObjectMapper mapper;
    private static final Logger LOG = LoggerFactory.getLogger(RedisCookbookDAO.class);

    public RedisCookbookDAO(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.mapper = new ObjectMapper();
    }

    private Set<String> getVersionsOfCookbook(String cookbookName) {
        Jedis redisClient = jedisPool.getResource();
        try {
            final String bucket = String.format("cookbook:%s", cookbookName);
            return redisClient.hkeys(bucket);
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }


    @Override
    public List<Cookbook> findAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            Set<String> cookbookNames = findAllCookbookNames();
            List<Cookbook> cookbooks = new ArrayList<>(cookbookNames.size());

            for(String cookbookName : cookbookNames) {
                for(String version : getVersionsOfCookbook(cookbookName)) {
                    Cookbook c = findByNameAndVersion(cookbookName, version);
                    if(c != null)
                        cookbooks.add(c);
                }
            }

            return cookbooks;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Set<String> findAllCookbookNames() {
        Jedis redisClient = jedisPool.getResource();
        try {
            return redisClient.zrange("cookbooks", 0, -1);
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void store(Cookbook cookbook) {
        Jedis redisClient = jedisPool.getResource();
        try {
            String json = mapper.writeValueAsString(cookbook);
            String bucket = String.format("cookbook:%s", cookbook.getCookbookName());
            redisClient.hset(bucket, cookbook.getVersion(), json);
            redisClient.zadd("cookbooks", 0, cookbook.getCookbookName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Set<Cookbook> removeByName(String cookbookName) {
        Set<Cookbook> removed = new HashSet<>();
        for(String version : getVersionsOfCookbook(cookbookName)) {
            Cookbook c = findByNameAndVersion(cookbookName, version);
            if (c != null) {
                removed.add(c);
            }
        }
        return removed;
    }

    @Override
    public void removeAll() {
        for(String cookbookName : findAllCookbookNames()) {
            removeByName(cookbookName);
        }
    }

    @Override
    public Set<Cookbook> findAllByName(String cookbookName) {
        Set<Cookbook> results = new HashSet<>();

        for(String version : getVersionsOfCookbook(cookbookName)) {
            results.add(findByNameAndVersion(cookbookName, version));
        }

        return results;
    }

    @Override
    public Cookbook findByNameAndVersion(String cookbookName, String version) {
        Jedis redisClient = jedisPool.getResource();
        try {
            String bucket = String.format("cookbook:%s", cookbookName);


            try {
                String cookbookJson = redisClient.hget(bucket, version);
                if(cookbookJson != null && cookbookJson.length() > 0)
                    return mapper.readValue(cookbookJson, Cookbook.class);
                else
                    return null;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Cookbook findLatestVersion(String cookbookName) {
        return CookbookFilter.latestVersion(findAllByName(cookbookName));
    }

    @Override
    // TODO: Abstract class some of these (like this one)
    public Set<Cookbook> findWithDependencies(String cookbookName, String version) {
        Cookbook root = findByNameAndVersion(cookbookName, version);
        Set<Cookbook> cookbooks = new HashSet<>();
        cookbooks.add(root);

        for(String depCookbookName : root.getDependencies().keySet()) {
            VersionConstraint constraint = root.getDependencies().get(depCookbookName);
            Cookbook bestMatch = findOneWithConstraints(depCookbookName, constraint, 1).get(0);
            cookbooks.addAll(findWithDependencies(depCookbookName, bestMatch.getVersion()));
        }

        return cookbooks;
    }

    @Override
    public Cookbook removeByNameAndVersion(String cookbookName, String cookbookVersion) {
        Cookbook removed = findByNameAndVersion(cookbookName, cookbookVersion);

        if (removed != null) {
            Jedis redisClient = jedisPool.getResource();
            try {
                String bucket = String.format("cookbook:%s", cookbookName);
                redisClient.hdel(bucket, cookbookVersion);
                // Last one? cleanup!
                if(redisClient.hkeys(bucket).size() == 0) {
                    redisClient.del(bucket);
                    redisClient.zrem("cookbooks", cookbookName);
                }
            } finally {
                jedisPool.returnResource(redisClient);
            }
        }

        return removed;
    }

    @Override
    public Map<String, List<Cookbook>> findAllWithConstraints(Map<String, VersionConstraint> constraintMap,
                                                              int numVersions) {

        return CookbookFilter.filterWithConstraintMap(findAll(), constraintMap, numVersions);
    }

    @Override
    public List<Cookbook> findOneWithConstraints(String cookbookName, VersionConstraint constraint, int numVersions) {
        return CookbookFilter.filterCookbooks(findAllByName(cookbookName), constraint, numVersions);
    }

    // TODO: make generic enough to remove - this should be moved out.
    @Override
    public List<Cookbook> findLatestVersions() {
        Set<String> cookbookNames = findAllCookbookNames();
        List<Cookbook> latestVersions = new ArrayList<>(cookbookNames.size());

        for(String cookbookName : cookbookNames) {
            Collection<Cookbook> cookbooks = findAllByName(cookbookName);
            Cookbook latest = CookbookFilter.latestVersion(cookbooks);
            if(latest != null) {
                latestVersions.add(latest);
            }
        }
        return latestVersions;
    }




}
