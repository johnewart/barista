package net.johnewart.barista.data.redis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.johnewart.barista.core.Node;
import net.johnewart.barista.data.NodeDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisNodeDAO implements NodeDAO {
    private final JedisPool jedisPool;
    private final ObjectMapper mapper;
    private static final Logger LOG = LoggerFactory.getLogger(RedisNodeDAO.class);

    public RedisNodeDAO(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.mapper = new ObjectMapper();
    }

    private Set<String> findAllNodeNames() {
        Jedis redisClient = jedisPool.getResource();
        try {
            return redisClient.zrange("nodes", 0, -1);
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public List<Node> findAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            Set<String> nodeNames = findAllNodeNames();
            List<Node> nodes = new ArrayList<>(nodeNames.size());

            for(String nodeName : nodeNames) {
                Node d = getByName(nodeName);
                if(d != null)
                    nodes.add(d);
            }

            return nodes;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void store(Node node) {
        Jedis redisClient = jedisPool.getResource();
        try {
            String json = mapper.writeValueAsString(node);
            String key = String.format("node:%s", node.getName());
            redisClient.set(key, json);
            redisClient.zadd("nodes", 0, node.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResource(redisClient);
        }    }

    @Override
    public Node removeByName(String nodeName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("node:%s", nodeName);
            Node node = getByName(nodeName);
            redisClient.del(key);
            redisClient.zrem("nodes", nodeName);
            return node;
        } finally {
            jedisPool.returnResource(redisClient);
        }    
    }

    @Override
    public void removeAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            for(String nodeName :  findAllNodeNames()) {
                removeByName(nodeName);
            }

        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Node getByName(String nodeName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("node:%s", nodeName);
            try {
                String json = redisClient.get(key);
                if(json != null && json.length() > 0)
                    return mapper.readValue(json, Node.class);
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
