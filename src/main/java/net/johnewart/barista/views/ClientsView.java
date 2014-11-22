package net.johnewart.barista.views;

import io.dropwizard.views.View;
import net.johnewart.barista.core.Client;
import net.johnewart.barista.data.ClientDAO;

import java.util.List;

public class ClientsView extends View {

    private final ClientDAO clientDAO;

    public ClientsView(ClientDAO clientDAO) {
        super("/views/ftl/clients.ftl");
        this.clientDAO = clientDAO;
    }

    public List<Client> getClients() {
        return clientDAO.findAll();
    }

}
