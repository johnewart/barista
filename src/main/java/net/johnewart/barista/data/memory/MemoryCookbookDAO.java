package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.SemanticVersion;
import net.johnewart.barista.core.VersionConstraint;
import net.johnewart.barista.data.CookbookDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryCookbookDAO implements CookbookDAO {
    private final ConcurrentHashMap<String, Map<String, Cookbook>> cookbookMap;

    public MemoryCookbookDAO() {
        cookbookMap = new ConcurrentHashMap<>();
    }

    @Override
    public List<Cookbook> findAll() {
        List<Cookbook> cookbooks = new LinkedList<>();

        for(Map<String, Cookbook> cookbookVersions : cookbookMap.values()) {
            cookbooks.addAll(cookbookVersions.values());
        }

        return ImmutableList.copyOf(cookbooks);
    }

    @Override
    public Set<String> findAllCookbookNames() {
        return new HashSet<>(cookbookMap.keySet());
    }

    @Override
    public void store(Cookbook cookbook) {
        final String cookbookName = cookbook.getCookbookName();

        if(!cookbookMap.containsKey(cookbookName)) {
            cookbookMap.put(cookbookName, new HashMap<String, Cookbook>());
        }

        cookbookMap.get(cookbookName).put(cookbook.getVersion(), cookbook);
    }

    @Override
    public Set<Cookbook> removeByName(String cookbookName) {
        Set<Cookbook> removed = new HashSet<>();

        Map<String, Cookbook> versionsRemoved = cookbookMap.remove(cookbookName);

        if(versionsRemoved != null) {
            removed.addAll(versionsRemoved.values());
        }

        return removed;
    }

    @Override
    public void removeAll() {
        cookbookMap.clear();
    }

    @Override
    public Set<Cookbook> findAllByName(String cookbookName) {
        Set<Cookbook> results = new HashSet<>();

        if(cookbookMap.containsKey(cookbookName)) {
            results.addAll(cookbookMap.get(cookbookName).values());
        }

        return results;
    }

    @Override
    public Cookbook findByNameAndVersion(String cookbookName, String version) {
        if(cookbookMap.containsKey(cookbookName)) {
            return cookbookMap.get(cookbookName).get(version);
        } else {
            return null;
        }
    }

    @Override
    public Cookbook removeByNameAndVersion(String cookbookName, String cookbookVersion) {

        if (cookbookMap.containsKey(cookbookName)) {
            Cookbook removed = cookbookMap.get(cookbookName).remove(cookbookVersion);

            if (cookbookMap.get(cookbookName).keySet().size() == 0) {
                cookbookMap.remove(cookbookName);
            }

            return removed;
        } else {
            return null;
        }

    }

    @Override
    public Map<String, List<Cookbook>> findAllWithConstraints(Map<String, VersionConstraint> constraintMap,
                                                              int numVersions) {
        Map<String, Set<Cookbook>> cookbooks = new HashMap<>();

        for (String cookbookName : cookbookMap.keySet()) {
            for (Cookbook cookbook : cookbookMap.get(cookbookName).values()) {
                final SemanticVersion cookbookVersion = cookbook.getSemanticVersion();
                final VersionConstraint constraint = constraintMap.get(cookbookName);

                if(!cookbooks.containsKey(cookbook.getCookbookName())) {
                    cookbooks.put(cookbookName, new HashSet<Cookbook>());
                }

                // No constraint? match!
                // Constraint with no previously found thing? match!
                if(constraint == null || constraint.matches(cookbookVersion)) {
                    cookbooks.get(cookbookName).add(cookbook);
                }
            }
        }

        Map<String, List<Cookbook>> results = new HashMap<>();
        for(String cookbookName : cookbooks.keySet()) {
            results.put(cookbookName, orderCookbooks(cookbooks.get(cookbookName), numVersions));
        }

        return results;
    }

    @Override
    public List<Cookbook> findOneWithConstraints(String cookbookName, VersionConstraint constraint, int numVersions) {
        Set<Cookbook> filtered = new HashSet<>();
        Map<String, List<Cookbook>> results = new HashMap<>();
        Set<Cookbook> cookbooks = findAllByName(cookbookName);

        for(Cookbook cookbook : cookbooks) {
            if(constraint == null || constraint.matches(cookbook.getSemanticVersion())) {
                filtered.add(cookbook);
            }
        }

        return orderCookbooks(filtered, numVersions);
    }

    @Override
    public List<Cookbook> findLatestVersions() {
        List<Cookbook> latestVersions = new ArrayList<>(cookbookMap.keySet().size());

        for(String cookbookName : cookbookMap.keySet()) {
            latestVersions.addAll(orderCookbooks(cookbookMap.get(cookbookName).values(), 1));
        }

        return latestVersions;
    }

    private List<Cookbook> orderCookbooks(Collection<Cookbook> cookbooks, final int numVersions) {
        List<Cookbook> cookbookList = new ArrayList<>(cookbooks);
        Collections.sort(cookbookList, new Comparator<Cookbook>() {
            @Override
            public int compare(Cookbook o1, Cookbook o2) {
                return o1.getSemanticVersion().compareTo(o2.getSemanticVersion());
            }
        });

        if (numVersions == CookbookDAO.NO_LIMITS) {
            return cookbookList;
        } else {
            int limit = Math.min(cookbookList.size(), numVersions);
            return cookbookList.subList(0, limit);
        }
    }


}
