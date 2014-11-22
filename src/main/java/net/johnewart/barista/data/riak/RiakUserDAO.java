package net.johnewart.barista.data.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import net.johnewart.barista.core.User;
import net.johnewart.barista.data.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class RiakUserDAO implements UserDAO {
    private final static Logger LOG = LoggerFactory.getLogger(RiakUserDAO.class);
    private final static Namespace USER_NAMESPACE = new Namespace("users");
    private final RiakCluster riakCluster;

    public RiakUserDAO(final RiakCluster riakCluster) {
        this.riakCluster = riakCluster;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new LinkedList<>();
        for(String userName : getAllUserNames()) {
            User user = getByName(userName);
            if(user == null) {
                LOG.warn("Null user for " + userName + "!");
            } else {
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public void store(User user) {
        try {

            Location userLocation = new Location(USER_NAMESPACE, user.getName());
            StoreValue storeUserOp = new StoreValue.Builder(user)
                    .withLocation(userLocation)
                    .build();

            RiakClient riakClient = new RiakClient(riakCluster);
            riakClient.execute(storeUserOp);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public User getByName(String userName) {
        try {
            Location cookbookLocation = new Location(USER_NAMESPACE, userName);

            RiakClient riakClient = new RiakClient(riakCluster);

            FetchValue fetchUserOp = new FetchValue.Builder(cookbookLocation)
                    .build();
            User fetchedUser = riakClient.execute(fetchUserOp).getValue(User.class);

            return fetchedUser;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public User removeByName(String userName) {
        Location userLocation = new Location(USER_NAMESPACE, userName);
        DeleteValue delete = new DeleteValue.Builder(userLocation).build();
        User user = getByName(userName);

        RiakClient riakClient = new RiakClient(riakCluster);
        try {
            riakClient.execute(delete);
        } catch (Exception e) {
            LOG.error("Error removing user " + userName + ": ", e);
        }

        return user;
    }

    @Override
    public void removeAll() {
        for(String userName : getAllUserNames()) {
            removeByName(userName);
        }
    }

    private List<String> getAllUserNames() {
        List<String> userNames = new LinkedList<>();

        try {
            RiakClient riakClient = new RiakClient(riakCluster);
            ListKeys lk = new ListKeys.Builder(USER_NAMESPACE).build();
            ListKeys.Response response = riakClient.execute(lk);
            for (Location l : response)
            {
                userNames.add(l.getKeyAsString());
            }
        } catch (Exception e) {
            LOG.error("Unable to remove all users!");
        }

        return userNames;
    }
}
