package net.johnewart.barista.data.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import net.johnewart.barista.core.Databag;
import net.johnewart.barista.data.DatabagDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class RiakDatabagDAO implements DatabagDAO {
    private final static Logger LOG = LoggerFactory.getLogger(RiakDatabagDAO.class);
    private final static Namespace DATABAGS_NAMESPACE = new Namespace("databags");
    private final RiakCluster riakCluster;

    public RiakDatabagDAO(final RiakCluster riakCluster) {
        this.riakCluster = riakCluster;
    }

    @Override
    public List<Databag> findAll() {
        List<Databag> databags = new LinkedList<>();
        for(String databagName : getAllDatabagNames()) {
            Databag databag = getByName(databagName);
            if(databag == null) {
                LOG.warn("Databag " + databagName + " was null!");
            } else {
                databags.add(databag);
            }
        }
        return databags;
    }

    @Override
    public void store(Databag databag) {
        try {

            Location databagLocation = new Location(DATABAGS_NAMESPACE, databag.getName());
            StoreValue storeDatabagOp = new StoreValue.Builder(databag)
                    .withLocation(databagLocation)
                    .build();

            RiakClient riakClient = new RiakClient(riakCluster);
            riakClient.execute(storeDatabagOp);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Databag getByName(String databagName) {
        try {
            Location cookbookLocation = new Location(DATABAGS_NAMESPACE, databagName);

            RiakClient riakClient = new RiakClient(riakCluster);

            FetchValue fetchDatabagOp = new FetchValue.Builder(cookbookLocation)
                    .build();
            Databag fetchedDatabag = riakClient.execute(fetchDatabagOp).getValue(Databag.class);

            return fetchedDatabag;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public Databag removeByName(String databagName) {
        Location cookbookLocation = new Location(DATABAGS_NAMESPACE, databagName);
        DeleteValue delete = new DeleteValue.Builder(cookbookLocation).build();
        Databag databag = getByName(databagName);

        RiakClient riakClient = new RiakClient(riakCluster);
        try {
            riakClient.execute(delete);
        } catch (Exception e) {
            LOG.error("Error removing databag " + databagName + ": ", e);
        }

        return databag;
    }

    @Override
    public void removeAll() {
        for(String databagName : getAllDatabagNames()) {
            removeByName(databagName);
        }
    }

    private List<String> getAllDatabagNames() {
        List<String> databagNames = new LinkedList<>();

        try {
            RiakClient riakClient = new RiakClient(riakCluster);
            ListKeys lk = new ListKeys.Builder(DATABAGS_NAMESPACE).build();
            ListKeys.Response response = riakClient.execute(lk);
            for (Location l : response)
            {
                databagNames.add(l.getKeyAsString());
            }
        } catch (Exception e) {
            LOG.error("Unable to remove all databags!");
        }

        return databagNames;
    }
}
