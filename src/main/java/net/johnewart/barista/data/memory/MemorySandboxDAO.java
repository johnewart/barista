package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import net.johnewart.barista.core.Sandbox;
import net.johnewart.barista.data.SandboxDAO;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.List;

public class MemorySandboxDAO implements SandboxDAO {
    private final ConcurrentHashSet<Sandbox> sandboxSet;

    public MemorySandboxDAO() {
        sandboxSet = new ConcurrentHashSet<>();
    }

    @Override
    public List<Sandbox> findAll() {
        return ImmutableList.copyOf(sandboxSet);
    }

    @Override
    public void add(Sandbox sandbox) {
        sandboxSet.add(sandbox);
    }

    @Override
    public Sandbox removeById(String sandboxId) {
        for (Sandbox sandbox : sandboxSet) {
            if (sandbox.getId().equals(sandboxId)) {
                sandboxSet.remove(sandbox);
                return sandbox;
            }
        }

        return null;
    }

    @Override
    public void removeAll() {
        sandboxSet.clear();
    }

    @Override
    public Sandbox getById(String sandboxId) {
        for (Sandbox sandbox : sandboxSet) {
            if (sandbox.getId().equals(sandboxId)) {
                return sandbox;
            }
        }

        return null;
    }
}
