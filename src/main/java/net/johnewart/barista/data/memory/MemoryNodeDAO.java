package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import net.johnewart.barista.core.Node;
import net.johnewart.barista.data.NodeDAO;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryNodeDAO implements NodeDAO {
    private final ConcurrentHashMap<String, Node> nodeMap;

    public MemoryNodeDAO() {
        nodeMap = new ConcurrentHashMap<>();
    }

    @Override
    public List<Node> findAll() {
        return ImmutableList.copyOf(nodeMap.values());
    }

    @Override
    public void store(Node node) {
        nodeMap.put(node.getName(), node);
    }

    @Override
    public Node removeByName(String nodeName) {
        return nodeMap.remove(nodeName);
    }

    @Override
    public void removeAll() {
        nodeMap.clear();
    }

    @Override
    public Node getByName(String nodeName) {
        if(nodeMap.containsKey(nodeName)) {
            return new Node(nodeMap.get(nodeName));
        } else {
            return null;
        }
    }
}
