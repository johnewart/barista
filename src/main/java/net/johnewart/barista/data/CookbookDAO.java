package net.johnewart.barista.data;

import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.Node;
import net.johnewart.barista.core.VersionConstraint;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CookbookDAO {
    List<Cookbook> findAll();
    void add(Cookbook cookbook);
    Cookbook getByName(String cookbookName);
    Set<Cookbook> removeByName(String cookbookName);
    void removeAll();
    Set<Cookbook> findAllByName(String cookbookName);
    boolean removeByNameAndVersion(String cookbookName, String cookbookVersion);
    Map<String, Set<Cookbook>> findAllWithConstraints(Map<String, VersionConstraint> constraintMap);
    Map<String, Set<Cookbook>> findOneWithConstraints(String cookbookName,  VersionConstraint constraint);
}
