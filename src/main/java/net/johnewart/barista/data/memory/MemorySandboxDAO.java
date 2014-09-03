package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import net.johnewart.barista.core.Sandbox;
import net.johnewart.barista.data.SandboxDAO;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MemorySandboxDAO implements SandboxDAO {
    private final ConcurrentHashMap<String, Sandbox> sandboxMap;

    public MemorySandboxDAO() {
        sandboxMap = new ConcurrentHashMap<>();
    }

    @Override
    public List<Sandbox> findAll() {
        return ImmutableList.copyOf(sandboxMap.values());
    }

    @Override
    public void store(Sandbox sandbox) {
        sandboxMap.put(sandbox.getId(), sandbox);
    }

    @Override
    public Sandbox removeById(String sandboxId) {
        return sandboxMap.remove(sandboxId);
    }

    @Override
    public void removeAll() {
        sandboxMap.clear();
    }

    @Override
    public Sandbox getById(String sandboxId) {
        if(sandboxMap.containsKey(sandboxId)) {
            return new Sandbox(sandboxMap.get(sandboxId));
        } else {
            return null;
        }
    }
}
