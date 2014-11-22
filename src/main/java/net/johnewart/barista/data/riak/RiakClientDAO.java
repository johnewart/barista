package net.johnewart.barista.data.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import net.johnewart.barista.core.Client;
import net.johnewart.barista.data.ClientDAO;

import java.util.LinkedList;
import java.util.List;

public class RiakClientDAO implements ClientDAO {
    private final RiakCluster cluster;

    public RiakClientDAO(RiakCluster cluster) {
        this.cluster = cluster;
    }

    @Override
    // TODO: This sucks performance-wise
    public List<Client> findAll() {
        List<Client> clients = new LinkedList<>();
        try {
            Namespace clientsBucket = new Namespace("clients");
            ListKeys listOp = new ListKeys.Builder(clientsBucket).build();
            RiakClient riakClient = new RiakClient(cluster);
            ListKeys.Response response = riakClient.execute(listOp);

            for (Location l : response)
            {
                clients.add(getByName(l.getKeyAsString()));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return clients;
    }

    @Override
    public void store(Client client) {
        try {

            Namespace clientsBucket = new Namespace("clients");
            Location clientLocation = new Location(clientsBucket, client.getName());
            StoreValue storeClientOp = new StoreValue.Builder(client)
                    .withLocation(clientLocation)
                    .build();

            RiakClient riakClient = new RiakClient(cluster);
            riakClient.execute(storeClientOp);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Client getByName(String clientName) {
        try {
            Namespace clientsBucket = new Namespace("clients");
            Location clientLocation = new Location(clientsBucket, clientName);

            RiakClient riakClient = new RiakClient(cluster);

            FetchValue fetchClientOp = new FetchValue.Builder(clientLocation)
                    .build();
            Client fetchedClient = riakClient.execute(fetchClientOp).getValue(Client.class);

            return fetchedClient;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public Client removeByName(String clientName) {
        try {
            Client c = getByName(clientName);
            Namespace clientsBucket = new Namespace("clients");
            Location clientLocation = new Location(clientsBucket, clientName);
            DeleteValue deleteOp = new DeleteValue.Builder(clientLocation)
                    .build();
            RiakClient riakClient = new RiakClient(cluster);
            riakClient.execute(deleteOp);
            return c;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public void removeAll() {
    }

}
