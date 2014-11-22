package net.johnewart.barista.data.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import net.johnewart.barista.core.Node;
import net.johnewart.barista.data.NodeDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class RiakNodeDAO implements NodeDAO {
    private final static Logger LOG = LoggerFactory.getLogger(RiakNodeDAO.class);
    private final static Namespace NODES_NAMESPACE = new Namespace("nodes");
    private final RiakCluster riakCluster;

    public RiakNodeDAO(final RiakCluster riakCluster) {
        this.riakCluster = riakCluster;
    }

    @Override
    public List<Node> findAll() {
        List<Node> nodes = new LinkedList<>();
        for(String nodeName : getAllNodeNames()) {
            nodes.add(getByName(nodeName));
        }
        return nodes;
    }

    @Override
    public void store(Node node) {
        try {

            Location nodeLocation = new Location(NODES_NAMESPACE, node.getName());
            StoreValue storeNodeOp = new StoreValue.Builder(node)
                    .withLocation(nodeLocation)
                    .build();

            RiakClient riakClient = new RiakClient(riakCluster);
            riakClient.execute(storeNodeOp);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Node getByName(String nodeName) {
        try {
            Location cookbookLocation = new Location(NODES_NAMESPACE, nodeName);

            RiakClient riakClient = new RiakClient(riakCluster);

            FetchValue fetchNodeOp = new FetchValue.Builder(cookbookLocation)
                    .build();
            Node fetchedNode = riakClient.execute(fetchNodeOp).getValue(Node.class);

            return fetchedNode;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public Node removeByName(String nodeName) {
        Location cookbookLocation = new Location(NODES_NAMESPACE, nodeName);
        DeleteValue delete = new DeleteValue.Builder(cookbookLocation).build();
        Node node = getByName(nodeName);

        RiakClient riakClient = new RiakClient(riakCluster);
        try {
            riakClient.execute(delete);
        } catch (Exception e) {
            LOG.error("Error removing node " + nodeName + ": ", e);
        }

        return node;
    }

    @Override
    public void removeAll() {
        for(String nodeName : getAllNodeNames()) {
            removeByName(nodeName);
        }
    }

    private List<String> getAllNodeNames() {
        List<String> nodeNames = new LinkedList<>();

        try {
            RiakClient riakClient = new RiakClient(riakCluster);
            ListKeys lk = new ListKeys.Builder(NODES_NAMESPACE).build();
            ListKeys.Response response = riakClient.execute(lk);
            for (Location l : response)
            {
                nodeNames.add(l.getKeyAsString());
            }
        } catch (Exception e) {
            LOG.error("Unable to remove all nodes!");
        }

        return nodeNames;
    }
}
