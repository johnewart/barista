package net.johnewart.barista.data.redis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.johnewart.barista.core.User;
import net.johnewart.barista.data.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisUserDAO implements UserDAO {
    private final JedisPool jedisPool;
    private final ObjectMapper mapper;
    private static final Logger LOG = LoggerFactory.getLogger(RedisUserDAO.class);

    public RedisUserDAO(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.mapper = new ObjectMapper();
    }

    private Set<String> findAllUserNames() {
        Jedis redisClient = jedisPool.getResource();
        try {
            return redisClient.zrange("users", 0, -1);
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public List<User> findAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            Set<String> userNames = findAllUserNames();
            List<User> users = new ArrayList<>(userNames.size());

            for(String userName : userNames) {
                User d = getByName(userName);
                if(d != null)
                    users.add(d);
            }

            return users;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void store(User user) {
        Jedis redisClient = jedisPool.getResource();
        try {
            String json = mapper.writeValueAsString(user);
            String key = String.format("user:%s", user.getName());
            redisClient.set(key, json);
            redisClient.zadd("users", 0, user.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResource(redisClient);
        }    }

    @Override
    public User removeByName(String userName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("user:%s", userName);
            User user = getByName(userName);
            redisClient.del(key);
            redisClient.zrem("users", userName);
            return user;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void removeAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            for(String userName :  findAllUserNames()) {
                removeByName(userName);
            }

        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public User getByName(String userName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("user:%s", userName);
            try {
                String json = redisClient.get(key);
                if(json != null && json.length() > 0)
                    return mapper.readValue(json, User.class);
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
