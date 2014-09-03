package net.johnewart.barista.data;

import net.johnewart.barista.core.Client;

import java.util.List;

public interface ClientDAO {
    List<Client> findAll();
    void store(Client client);
    Client getByName(String clientName);
    Client removeByName(String clientName);
    void removeAll();
}
