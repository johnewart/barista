package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.SemanticVersion;
import net.johnewart.barista.core.VersionConstraint;
import net.johnewart.barista.data.CookbookDAO;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.*;

public class MemoryCookbookDAO implements CookbookDAO {
    private final ConcurrentHashSet<Cookbook> cookbookSet;

    public MemoryCookbookDAO() {
        cookbookSet = new ConcurrentHashSet<>();
    }

    @Override
    public List<Cookbook> findAll() {
        return ImmutableList.copyOf(cookbookSet);
    }

    @Override
    public void add(Cookbook cookbook) {
        cookbookSet.add(cookbook);
    }

    @Override
    public Set<Cookbook> removeByName(String cookbookName) {
        Set<Cookbook> toRemove = new HashSet<>();

        for (Cookbook cookbook : cookbookSet) {
            if (cookbook.getCookbookName().equals(cookbookName)) {
                toRemove.add(cookbook);

            }
        }

        ImmutableSet removed = ImmutableSet.copyOf(toRemove);
        cookbookSet.removeAll(toRemove);
        return removed;
    }

    @Override
    public void removeAll() {
        cookbookSet.clear();
    }

    @Override
    public Set<Cookbook> findAllByName(String cookbookName) {
        Set<Cookbook> results = new HashSet<>();

        for (Cookbook cookbook : cookbookSet) {
            if (cookbook.getCookbookName().equals(cookbookName)) {
                results.add(cookbook);
            }
        }

        return results;
    }

    @Override
    public boolean removeByNameAndVersion(String cookbookName, String cookbookVersion) {

        for (Cookbook cookbook : cookbookSet) {
            if (cookbook.getCookbookName().equals(cookbookName) &&
                cookbook.getVersion().equals(cookbookVersion)) {
                cookbookSet.remove(cookbook);
                return true;
            }
        }

        return false;
    }

    @Override
    public Map<String, Set<Cookbook>> findAllWithConstraints(Map<String, VersionConstraint> constraintMap) {
        Map<String, Set<Cookbook>> cookbooks = new HashMap<>();

        for (Cookbook cookbook : cookbookSet) {
            final String cookbookName = cookbook.getCookbookName();
            final SemanticVersion cookbookVersion = cookbook.getSemanticVersion();
            final VersionConstraint constraint = constraintMap.get(cookbookName);

            if(!cookbooks.containsKey(cookbook.getCookbookName())) {
                cookbooks.put(cookbookName, new HashSet<Cookbook>());
            }

            if(constraint == null || constraint.matches(cookbookVersion)) {
                cookbooks.get(cookbookName).add(cookbook);
            }
        }

        return cookbooks;
    }

    @Override
    public Map<String, Set<Cookbook>> findOneWithConstraints(String cookbookName, VersionConstraint constraint) {
        Set<Cookbook> filtered = new HashSet<>();
        Map<String, Set<Cookbook>> results = new HashMap<>();
        Set<Cookbook> cookbooks = findAllByName(cookbookName);

        for(Cookbook cookbook : cookbooks) {
            if(constraint.matches(cookbook.getSemanticVersion())) {
                filtered.add(cookbook);
            }
        }

        results.put(cookbookName, filtered);

        return results;

    }


    @Override
    public Cookbook getByName(String cookbookName) {
        for (Cookbook cookbook : cookbookSet) {
            if (cookbook.getName().equals(cookbookName)) {
                return cookbook;
            }
        }

        return null;
    }
}
