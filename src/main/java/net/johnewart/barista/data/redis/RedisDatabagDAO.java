package net.johnewart.barista.data.redis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.johnewart.barista.core.Client;
import net.johnewart.barista.core.Databag;
import net.johnewart.barista.data.DatabagDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RedisDatabagDAO implements DatabagDAO {

    private final JedisPool jedisPool;
    private final ObjectMapper mapper;
    private static final Logger LOG = LoggerFactory.getLogger(RedisDatabagDAO.class);

    public RedisDatabagDAO(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<Databag> findAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            Set<String> databagNames = findAllDatabagNames();
            List<Databag> databags = new ArrayList<>(databagNames.size());

            for(String databagName : databagNames) {
                Databag d = getByName(databagName);
                if(d != null)
                    databags.add(d);
            }

            return databags;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void store(Databag databag) {
        Jedis redisClient = jedisPool.getResource();
        try {
            String json = mapper.writeValueAsString(databag);
            String key = String.format("databag:%s", databag.getName());
            redisClient.set(key, json);
            redisClient.zadd("databags", 0, databag.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Databag getByName(String databagName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("databag:%s", databagName);
            try {
                String json = redisClient.get(key);
                if(json != null && json.length() > 0)
                    return mapper.readValue(json, Databag.class);
                else
                    return null;
            } catch (JsonMappingException e) {
                LOG.error("Error mapping JSON ", e);
            } catch (JsonParseException e) {
                LOG.error("Error parsing JSON ", e);
            } catch (IOException e) {
                LOG.error("I/O Error ", e);
            }

            return null;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Databag removeByName(String databagName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("databag:%s", databagName);
            Databag databag = getByName(databagName);
            redisClient.del(key);
            redisClient.zrem("databags", databagName);
            return databag;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void removeAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            for(String databagName :  findAllDatabagNames()) {
                removeByName(databagName);
            }

        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    private Set<String> findAllDatabagNames() {
        Jedis redisClient = jedisPool.getResource();
        try {
            return redisClient.zrange("databags", 0, -1);
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }
}
