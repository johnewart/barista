package net.johnewart.barista.data.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import net.johnewart.barista.core.Environment;
import net.johnewart.barista.data.EnvironmentDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class RiakEnvironmentDAO implements EnvironmentDAO {
    private final static Logger LOG = LoggerFactory.getLogger(RiakEnvironmentDAO.class);
    private final static Namespace ENVIRONMENTS_NAMESPACE = new Namespace("environments");
    private final RiakCluster riakCluster;

    public RiakEnvironmentDAO(final RiakCluster riakCluster) {
        this.riakCluster = riakCluster;
    }

    @Override
    public List<Environment> findAll() {
        List<Environment> environments = new LinkedList<>();
        for(String environmentName : getAllEnvironmentNames()) {
            environments.add(getByName(environmentName));
        }
        return environments;
    }

    @Override
    public void store(Environment environment) {
        try {

            Location environmentLocation = new Location(ENVIRONMENTS_NAMESPACE, environment.getName());
            StoreValue storeEnvironmentOp = new StoreValue.Builder(environment)
                    .withLocation(environmentLocation)
                    .build();

            RiakClient riakClient = new RiakClient(riakCluster);
            riakClient.execute(storeEnvironmentOp);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Environment getByName(String environmentName) {
        try {
            Location cookbookLocation = new Location(ENVIRONMENTS_NAMESPACE, environmentName);

            RiakClient riakClient = new RiakClient(riakCluster);

            FetchValue fetchEnvironmentOp = new FetchValue.Builder(cookbookLocation)
                    .build();
            Environment fetchedEnvironment = riakClient.execute(fetchEnvironmentOp).getValue(Environment.class);

            return fetchedEnvironment;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public Environment removeByName(String environmentName) {
        Location cookbookLocation = new Location(ENVIRONMENTS_NAMESPACE, environmentName);
        DeleteValue delete = new DeleteValue.Builder(cookbookLocation).build();
        Environment environment = getByName(environmentName);

        RiakClient riakClient = new RiakClient(riakCluster);
        try {
            riakClient.execute(delete);
        } catch (Exception e) {
            LOG.error("Error removing environment " + environmentName + ": ", e);
        }

        return environment;
    }

    @Override
    public void removeAll() {
        for(String environmentName : getAllEnvironmentNames()) {
            removeByName(environmentName);
        }
    }

    private List<String> getAllEnvironmentNames() {
        List<String> environmentNames = new LinkedList<>();

        try {
            RiakClient riakClient = new RiakClient(riakCluster);
            ListKeys lk = new ListKeys.Builder(ENVIRONMENTS_NAMESPACE).build();
            ListKeys.Response response = riakClient.execute(lk);
            for (Location l : response)
            {
                environmentNames.add(l.getKeyAsString());
            }
        } catch (Exception e) {
            LOG.error("Unable to remove all environments!");
        }

        return environmentNames;
    }
}
