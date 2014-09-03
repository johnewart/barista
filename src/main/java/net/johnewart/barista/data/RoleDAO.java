package net.johnewart.barista.data;

import net.johnewart.barista.core.Role;

import java.util.List;

public interface RoleDAO {
    List<Role> findAll();
    void store(Role role);
    Role getByName(String roleName);
    Role removeByName(String roleName);
    void removeAll();
}
