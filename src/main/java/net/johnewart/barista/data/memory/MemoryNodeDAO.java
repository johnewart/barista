package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import net.johnewart.barista.core.Node;
import net.johnewart.barista.data.NodeDAO;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.List;

public class MemoryNodeDAO implements NodeDAO {
    private final ConcurrentHashSet<Node> nodeSet;

    public MemoryNodeDAO() {
        nodeSet = new ConcurrentHashSet<>();
    }

    @Override
    public List<Node> findAll() {
        return ImmutableList.copyOf(nodeSet);
    }

    @Override
    public void add(Node node) {
        nodeSet.add(node);
    }

    @Override
    public Node removeByName(String nodeName) {
        for (Node node : nodeSet) {
            if (node.getName().equals(nodeName)) {
                nodeSet.remove(node);
                return node;
            }
        }

        return null;
    }

    @Override
    public void removeAll() {
        nodeSet.clear();
    }

    @Override
    public Node getByName(String nodeName) {
        for (Node node : nodeSet) {
            if (node.getName().equals(nodeName)) {
                return node;
            }
        }

        return null;
    }
}
