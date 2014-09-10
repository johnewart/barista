package net.johnewart.barista.data.redis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.johnewart.barista.core.Client;
import net.johnewart.barista.data.ClientDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisClientDAO implements ClientDAO {
    private final JedisPool jedisPool;
    private final ObjectMapper mapper;
    private static final Logger LOG = LoggerFactory.getLogger(RedisClientDAO.class);

    public RedisClientDAO(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<Client> findAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            Set<String> clientNames = redisClient.zrange("clients", 0, -1);
            List<Client> clients = new ArrayList<>(clientNames.size());

            for(String clientName : clientNames) {
                Client c = getByName(clientName);
                if(c != null)
                    clients.add(c);
            }

            return clients;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void store(Client client) {
        Jedis redisClient = jedisPool.getResource();
        try {
            String json = mapper.writeValueAsString(client);
            String key = String.format("client:%s", client.getName());
            redisClient.set(key, json);
            redisClient.zadd("clients", 0, client.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Client removeByName(String clientName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("client:%s", clientName);
            Client client = getByName(clientName);
            redisClient.del(key);
            redisClient.zrem("clients", clientName);
            return client;
        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public void removeAll() {
        Jedis redisClient = jedisPool.getResource();
        try {
            Set<String> clientNames = redisClient.zrange("clients", 0, -1);

            for(String clientName : clientNames) {
                removeByName(clientName);
            }

        } finally {
            jedisPool.returnResource(redisClient);
        }
    }

    @Override
    public Client getByName(String clientName) {
        Jedis redisClient = jedisPool.getResource();

        try {
            String key = String.format("client:%s", clientName);
            try {
                String clientJson = redisClient.get(String.format("client:%s", clientName));
                if(clientJson != null && clientJson.length() > 0)
                    return mapper.readValue(clientJson, Client.class);
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
