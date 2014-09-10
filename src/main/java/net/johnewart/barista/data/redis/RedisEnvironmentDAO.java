package net.johnewart.barista.data.redis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.johnewart.barista.core.Environment;
import net.johnewart.barista.data.EnvironmentDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisEnvironmentDAO implements EnvironmentDAO {
    private final JedisPool jedisPool;
    private final ObjectMapper mapper;
    private static final Logger LOG = LoggerFactory.getLogger(RedisEnvironmentDAO.class);


    public RedisEnvironmentDAO(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.mapper = new ObjectMapper();
    }

    private Set<String> findAllEnvironmentNames() {
        Jedis redisClient = jedisPool.getResource();
        try {
            return redisClient.zrange("environments", 0, -1);
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public List<Environment> findAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            Set<String> environmentNames = findAllEnvironmentNames();
            List<Environment> environments = new ArrayList<>(environmentNames.size());

            for(String environmentName : environmentNames) {
                Environment e = getByName(environmentName);
                if(e != null)
                    environments.add(e);
            }

            return environments;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void store(Environment environment) {
        Jedis redisClient = jedisPool.getResource();
        try {
            String json = mapper.writeValueAsString(environment);
            String key = String.format("environment:%s", environment.getName());
            redisClient.set(key, json);
            redisClient.zadd("environments", 0, environment.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Environment removeByName(String environmentName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("environment:%s", environmentName);
            Environment environment = getByName(environmentName);
            redisClient.del(key);
            redisClient.zrem("environments", environmentName);
            return environment;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void removeAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            for(String databagName :  findAllEnvironmentNames()) {
                removeByName(databagName);
            }
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Environment getByName(String environmentName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            final String key = String.format("environment:%s", environmentName);
            try {
                String json = redisClient.get(key);
                if(json != null && json.length() > 0)
                    return mapper.readValue(json, Environment.class);
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

}
