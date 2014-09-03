package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import net.johnewart.barista.core.Environment;
import net.johnewart.barista.data.EnvironmentDAO;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryEnvironmentDAO implements EnvironmentDAO {
    private final ConcurrentHashMap<String, Environment> environmentMap;

    public MemoryEnvironmentDAO() {
        environmentMap = new ConcurrentHashMap<>();
        // Initialize _default environment
        Environment defaultEnv = new Environment("_default");
        environmentMap.put("_default", defaultEnv);
    }

    @Override
    public List<Environment> findAll() {
        return ImmutableList.copyOf(environmentMap.values());
    }

    @Override
    public void store(Environment environment) {
        environmentMap.put(environment.getName(), environment);
    }

    @Override
    public Environment removeByName(String environmentName) {
        return environmentMap.remove(environmentName);
    }

    @Override
    public void removeAll() {
        environmentMap.clear();
        environmentMap.put("_default", new Environment("_default"));
    }

    @Override
    public Environment getByName(String environmentName) {
        Environment existing = environmentMap.get(environmentName);

        if(existing == null) {
            return null;
        } else {
            return new Environment(existing);
        }
    }

}
