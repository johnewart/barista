package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import net.johnewart.barista.core.Environment;
import net.johnewart.barista.data.EnvironmentDAO;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.List;

public class MemoryEnvironmentDAO implements EnvironmentDAO {
    private final ConcurrentHashSet<Environment> environmentSet;

    public MemoryEnvironmentDAO() {
        environmentSet = new ConcurrentHashSet<>();
    }

    @Override
    public List<Environment> findAll() {
        return ImmutableList.copyOf(environmentSet);
    }

    @Override
    public void add(Environment environment) {
        environmentSet.add(environment);
    }

    @Override
    public Environment removeByName(String environmentName) {
        for (Environment environment : environmentSet) {

            if (environment != null &&
                environment.getName() != null &&
                environment.getName().equals(environmentName))
            {
                environmentSet.remove(environment);
                return environment;
            }

        }

        return null;
    }

    @Override
    public void removeAll() {
        environmentSet.clear();
    }

    @Override
    public Environment getByName(String environmentName) {
        for (Environment environment : environmentSet) {
            if (environment != null &&
                environment.getName() != null &&
                environment.getName().equals(environmentName))
            {
                return environment;
            }
        }

        return null;
    }
}
