package net.johnewart.barista.data.memory;

import net.johnewart.barista.core.Databag;
import net.johnewart.barista.data.DatabagDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryDatabagDAO implements DatabagDAO {

    final ConcurrentHashMap<String, Databag> databagMap;

    public MemoryDatabagDAO() {
        this.databagMap = new ConcurrentHashMap<>();
    }

    @Override
    public List<Databag> findAll() {
        return new ArrayList<>(databagMap.values());
    }

    @Override
    public void store(Databag databag) {
        databagMap.put(databag.getName(), databag);
    }

    @Override
    public Databag getByName(String databagName) {
        if(databagMap.containsKey(databagName)) {
            return new Databag(databagMap.get(databagName));
        } else {
            return null;
        }
    }

    @Override
    public Databag removeByName(String databagName) {
        return databagMap.remove(databagName);
    }

    @Override
    public void removeAll() {
        databagMap.clear();
    }
}
