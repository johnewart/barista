package net.johnewart.barista.data.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import net.johnewart.barista.core.Role;
import net.johnewart.barista.data.RoleDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class RiakRoleDAO implements RoleDAO {
    private final static Logger LOG = LoggerFactory.getLogger(RiakRoleDAO.class);
    private final static Namespace ROLES_NAMESPACE = new Namespace("roles");
    private final RiakCluster riakCluster;

    public RiakRoleDAO(final RiakCluster riakCluster) {
        this.riakCluster = riakCluster;
    }

    @Override
    public List<Role> findAll() {
        List<Role> roles = new LinkedList<>();
        for(String roleName : getAllRoleNames()) {
            Role role = getByName(roleName);
            if(role == null) {
                LOG.warn("Role " + roleName + " was null");
            } else {
                roles.add(role);
            }
        }
        return roles;
    }

    @Override
    public void store(Role role) {
        try {

            Location roleLocation = new Location(ROLES_NAMESPACE, role.getName());
            StoreValue storeRoleOp = new StoreValue.Builder(role)
                    .withLocation(roleLocation)
                    .build();

            RiakClient riakClient = new RiakClient(riakCluster);
            riakClient.execute(storeRoleOp);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Role getByName(String roleName) {
        try {
            Location cookbookLocation = new Location(ROLES_NAMESPACE, roleName);

            RiakClient riakClient = new RiakClient(riakCluster);

            FetchValue fetchRoleOp = new FetchValue.Builder(cookbookLocation)
                    .build();
            Role fetchedRole = riakClient.execute(fetchRoleOp).getValue(Role.class);

            return fetchedRole;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public Role removeByName(String roleName) {
        Location cookbookLocation = new Location(ROLES_NAMESPACE, roleName);
        DeleteValue delete = new DeleteValue.Builder(cookbookLocation).build();
        Role role = getByName(roleName);

        RiakClient riakClient = new RiakClient(riakCluster);
        try {
            riakClient.execute(delete);
        } catch (Exception e) {
            LOG.error("Error removing role " + roleName + ": ", e);
        }

        return role;
    }

    @Override
    public void removeAll() {
        for(String roleName : getAllRoleNames()) {
            removeByName(roleName);
        }
    }

    private List<String> getAllRoleNames() {
        List<String> roleNames = new LinkedList<>();

        try {
            RiakClient riakClient = new RiakClient(riakCluster);
            ListKeys lk = new ListKeys.Builder(ROLES_NAMESPACE).build();
            ListKeys.Response response = riakClient.execute(lk);
            for (Location l : response)
            {
                roleNames.add(l.getKeyAsString());
            }
        } catch (Exception e) {
            LOG.error("Unable to remove all roles!");
        }

        return roleNames;
    }
}
