package net.johnewart.barista.data;

import net.johnewart.barista.core.Node;

import java.util.List;

public interface NodeDAO {
    List<Node> findAll();
    void store(Node node);
    Node getByName(String nodeName);
    Node removeByName(String nodeName);
    void removeAll();
}
