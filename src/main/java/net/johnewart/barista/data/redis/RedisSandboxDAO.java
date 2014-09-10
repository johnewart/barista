package net.johnewart.barista.data.redis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.johnewart.barista.core.Sandbox;
import net.johnewart.barista.data.SandboxDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisSandboxDAO implements SandboxDAO {
    private final JedisPool jedisPool;
    private final ObjectMapper mapper;
    private static final Logger LOG = LoggerFactory.getLogger(RedisSandboxDAO.class);

    public RedisSandboxDAO(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.mapper = new ObjectMapper();
    }

    private Set<String> findAllSandboxIds() {
        Jedis redisClient = jedisPool.getResource();
        try {
            return redisClient.zrange("sandboxes", 0, -1);
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public List<Sandbox> findAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            Set<String> sandboxIds = findAllSandboxIds();
            List<Sandbox> sandboxes = new ArrayList<>(sandboxIds.size());

            for(String sandboxId : sandboxIds) {
                Sandbox d = getById(sandboxId);
                if(d != null)
                    sandboxes.add(d);
            }

            return sandboxes;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void store(Sandbox sandbox) {
        Jedis redisClient = jedisPool.getResource();
        try {
            String json = mapper.writeValueAsString(sandbox);
            String key = String.format("sandbox:%s", sandbox.getId());
            redisClient.set(key, json);
            redisClient.zadd("sandboxes", 0, sandbox.getId());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResource(redisClient);
        }    }


    @Override
    public Sandbox removeById(String sandboxId) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("sandbox:%s", sandboxId);
            Sandbox sandbox = getById(sandboxId);
            redisClient.del(key);
            redisClient.zrem("sandboxes", sandboxId);
            return sandbox;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void removeAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            for(String sandboxId :  findAllSandboxIds()) {
                removeById(sandboxId);
            }

        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Sandbox getById(String sandboxId) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("sandbox:%s", sandboxId);
            try {
                String json = redisClient.get(key);
                if(json != null && json.length() > 0)
                    return mapper.readValue(json, Sandbox.class);
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
