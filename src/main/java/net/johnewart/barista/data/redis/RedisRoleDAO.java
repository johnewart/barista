package net.johnewart.barista.data.redis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.johnewart.barista.core.Role;
import net.johnewart.barista.data.RoleDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisRoleDAO implements RoleDAO {
    private final JedisPool jedisPool;
    private final ObjectMapper mapper;
    private static final Logger LOG = LoggerFactory.getLogger(RedisRoleDAO.class);

    public RedisRoleDAO(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.mapper = new ObjectMapper();
    }

    private Set<String> findAllRoleNames() {
        Jedis redisClient = jedisPool.getResource();
        try {
            return redisClient.zrange("roles", 0, -1);
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public List<Role> findAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            Set<String> roleNames = findAllRoleNames();
            List<Role> roles = new ArrayList<>(roleNames.size());

            for(String roleName : roleNames) {
                Role d = getByName(roleName);
                if(d != null)
                    roles.add(d);
            }

            return roles;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void store(Role role) {
        Jedis redisClient = jedisPool.getResource();
        try {
            String json = mapper.writeValueAsString(role);
            String key = String.format("role:%s", role.getName());
            redisClient.set(key, json);
            redisClient.zadd("roles", 0, role.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResource(redisClient);
        }    }

    @Override
    public Role removeByName(String roleName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("role:%s", roleName);
            Role role = getByName(roleName);
            redisClient.del(key);
            redisClient.zrem("roles", roleName);
            return role;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void removeAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            for(String roleName :  findAllRoleNames()) {
                removeByName(roleName);
            }

        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Role getByName(String roleName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("role:%s", roleName);
            try {
                String json = redisClient.get(key);
                if(json != null && json.length() > 0)
                    return mapper.readValue(json, Role.class);
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
