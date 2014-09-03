package net.johnewart.barista.data;

import net.johnewart.barista.core.Databag;

import java.util.List;

public interface DatabagDAO {
    List<Databag> findAll();
    void store(Databag databag);
    Databag getByName(String databagName);
    Databag removeByName(String databagName);
    void removeAll();
}
