package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import net.johnewart.barista.core.Role;
import net.johnewart.barista.data.RoleDAO;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryRoleDAO implements RoleDAO {
    private final ConcurrentHashMap<String, Role> roleMap;

    public MemoryRoleDAO() {
        roleMap = new ConcurrentHashMap<>();
    }

    @Override
    public List<Role> findAll() {
        return ImmutableList.copyOf(roleMap.values());
    }

    @Override
    public void store(Role role) {
        roleMap.put(role.getName(), new Role(role));
    }

    @Override
    public Role removeByName(String roleName) {
        return roleMap.remove(roleName);
    }

    @Override
    public void removeAll() {
        roleMap.clear();
    }

    @Override
    public Role getByName(String roleName) {
        Role thing = roleMap.get(roleName);
        if(thing != null)
            return new Role(thing);
        else
            return null;
    }
}
