package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import net.johnewart.barista.core.Client;
import net.johnewart.barista.data.ClientDAO;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryClientDAO implements ClientDAO {
    private final ConcurrentHashMap<String, Client> clientMap;

    public MemoryClientDAO() {
        clientMap = new ConcurrentHashMap<>();
        Client adminClient = new Client("admin");
        clientMap.put("admin", adminClient);
        Client webuiClient = new Client("chef-webui");
        clientMap.put("chef-webui", webuiClient);
        Client validator = new Client("chef-validator");
        clientMap.put("chef-validator", validator);


    }

    @Override
    public List<Client> findAll() {
        return ImmutableList.copyOf(clientMap.values());
    }

    @Override
    public void store(Client client) {
        clientMap.put(client.getName(), client);
    }

    @Override
    public Client removeByName(String clientName) {
        return clientMap.remove(clientName);
    }

    @Override
    public void removeAll() {
        clientMap.clear();
    }

    @Override
    public Client getByName(String clientName) {
        Client client = clientMap.get(clientName);
        if(client != null) {
            return new Client(client);
        } else {
            return null;
        }
    }
}
