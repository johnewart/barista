package net.johnewart.barista.data;

import net.johnewart.barista.core.Environment;
import net.johnewart.barista.core.Node;

import java.util.List;

public interface EnvironmentDAO {
    List<Environment> findAll();
    void store(Environment environment);
    Environment getByName(String envName);
    Environment removeByName(String envName);
    void removeAll();
}
