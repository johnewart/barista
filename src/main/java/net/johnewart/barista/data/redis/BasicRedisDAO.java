package net.johnewart.barista.data.redis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BasicRedisDAO<T> {
    private final JedisPool jedisPool;
    private final ObjectMapper mapper;
    private static final Logger LOG = LoggerFactory.getLogger(BasicRedisDAO.class);

    protected abstract String getKeyPrefix();
    
    public BasicRedisDAO(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.mapper = new ObjectMapper();
    }

    public List<T> findAll() {
        Jedis redisT = jedisPool.getResource();
        try {
            Set<String> thingNames = redisT.zrange("things", 0, -1);
            List<T> things = new ArrayList(thingNames.size());

            for(String thingName : thingNames) {
                T c = getByName(thingName);
                if(c != null)
                    things.add(c);
            }

            return things;
        } finally {
            jedisPool.returnResource(redisT);
        }
    }

    public void store(T thing) {
        Jedis redisT = jedisPool.getResource();
        try {
            String json = mapper.writeValueAsString(thing);
            //String key = String.format("%s:%s",  getKeyPrefix(), T.getKey());
            //redisT.set(key, json);
            //redisT.zadd("things", 0, thing.getKey());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResource(redisT);
        }
    }

    public T removeByName(String thingName) {
        Jedis redisT = jedisPool.getResource();

        try {
            String key = String.format("thing:%s", thingName);
            T thing = getByName(thingName);
            redisT.del(key);
            redisT.zrem("things", thingName);
            return thing;
        } finally {
            jedisPool.returnResource(redisT);
        }
    }

    public void removeAll() {
        Jedis redisT = jedisPool.getResource();
        try {
            Set<String> thingNames = redisT.zrange("things", 0, -1);

            for(String thingName : thingNames) {
                removeByName(thingName);
            }

        } finally {
            jedisPool.returnResource(redisT);
        }
    }

    public T getByName(String thingName) {
        Jedis redisT = jedisPool.getResource();

        try {
            String key = String.format("thing:%s", thingName);
            //ry {
                String thingJson = redisT.get(String.format("thing:%s", thingName));
                //if(thingJson != null && thingJson.length() > 0)
                    //return mapper.readValue(thingJson, );
                //else
                    return null;
            /*} /*catch (JsonMappingException e) {
                LOG.error("Error mapping JSON ", e);
            } catch (JsonParseException e) {
                LOG.error("Error parsing JSON ", e);
            } catch (IOException e) {
                LOG.error("I/O Error ", e);
            }   */

            //return null;
        } finally {
            jedisPool.returnResource(redisT);
        }
    }
}
