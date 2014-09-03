package net.johnewart.barista.data;

import net.johnewart.barista.core.Sandbox;

import java.util.List;

public interface SandboxDAO {
    List<Sandbox> findAll();
    void store(Sandbox sandbox);
    Sandbox getById(String sandboxId);
    Sandbox removeById(String sandboxId);
    void removeAll();
}
