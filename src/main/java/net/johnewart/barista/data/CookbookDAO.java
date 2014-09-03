package net.johnewart.barista.data;

import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.Node;
import net.johnewart.barista.core.VersionConstraint;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CookbookDAO {
    int NO_LIMITS = -1;

    List<Cookbook> findAll();
    Set<String> findAllCookbookNames();

    void store(Cookbook cookbook);
    void removeAll();

    Set<Cookbook> findAllByName(String cookbookName);
    Cookbook findByNameAndVersion(String cookbookName, String version);

    Set<Cookbook> removeByName(String cookbookName);
    Cookbook removeByNameAndVersion(String cookbookName, String cookbookVersion);

    Map<String, List<Cookbook>> findAllWithConstraints(Map<String, VersionConstraint> constraintMap, int numVersions);
    List<Cookbook> findOneWithConstraints(String cookbookName, VersionConstraint constraint, int numVersions);

    List<Cookbook> findLatestVersions();
}
