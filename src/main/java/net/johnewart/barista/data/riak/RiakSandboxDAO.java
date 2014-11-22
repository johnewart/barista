package net.johnewart.barista.data.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import net.johnewart.barista.core.Sandbox;
import net.johnewart.barista.data.SandboxDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class RiakSandboxDAO implements SandboxDAO {
    private final static Logger LOG = LoggerFactory.getLogger(RiakSandboxDAO.class);
    private final static Namespace NODES_NAMESPACE = new Namespace("sandboxes");
    private final RiakCluster riakCluster;

    public RiakSandboxDAO(final RiakCluster riakCluster) {
        this.riakCluster = riakCluster;
    }

    @Override
    public List<Sandbox> findAll() {
        List<Sandbox> sandboxs = new LinkedList<>();
        for(String sandboxId : getAllSandboxIds()) {
            sandboxs.add(getById(sandboxId));
        }
        return sandboxs;
    }

    @Override
    public void store(Sandbox sandbox) {
        try {

            Location sandboxLocation = new Location(NODES_NAMESPACE, sandbox.getName());
            StoreValue storeSandboxOp = new StoreValue.Builder(sandbox)
                    .withLocation(sandboxLocation)
                    .build();

            RiakClient riakClient = new RiakClient(riakCluster);
            riakClient.execute(storeSandboxOp);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Sandbox getById(String sandboxId) {
        try {
            Location cookbookLocation = new Location(NODES_NAMESPACE, sandboxId);

            RiakClient riakClient = new RiakClient(riakCluster);

            FetchValue fetchSandboxOp = new FetchValue.Builder(cookbookLocation)
                    .build();
            Sandbox fetchedSandbox = riakClient.execute(fetchSandboxOp).getValue(Sandbox.class);

            return fetchedSandbox;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public Sandbox removeById(String sandboxId) {
        Location cookbookLocation = new Location(NODES_NAMESPACE, sandboxId);
        DeleteValue delete = new DeleteValue.Builder(cookbookLocation).build();
        Sandbox sandbox = getById(sandboxId);

        RiakClient riakClient = new RiakClient(riakCluster);
        try {
            riakClient.execute(delete);
        } catch (Exception e) {
            LOG.error("Error removing sandbox " + sandboxId + ": ", e);
        }

        return sandbox;
    }

    @Override
    public void removeAll() {
        for(String sandboxId : getAllSandboxIds()) {
            removeById(sandboxId);
        }
    }

    private List<String> getAllSandboxIds() {
        List<String> sandboxNames = new LinkedList<>();

        try {
            RiakClient riakClient = new RiakClient(riakCluster);
            ListKeys lk = new ListKeys.Builder(NODES_NAMESPACE).build();
            ListKeys.Response response = riakClient.execute(lk);
            for (Location l : response)
            {
                sandboxNames.add(l.getKeyAsString());
            }
        } catch (Exception e) {
            LOG.error("Unable to remove all sandboxs!");
        }

        return sandboxNames;
    }
}
