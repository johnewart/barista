package net.johnewart.barista.core.cookbook;


import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.SemanticVersion;
import net.johnewart.barista.core.VersionConstraint;
import net.johnewart.barista.data.CookbookDAO;

import java.util.*;

public class CookbookFilter {
    public static List<Cookbook> filterCookbooks(Set<Cookbook> cookbooks, VersionConstraint constraint, int numVersions) {
        Set<Cookbook> filtered = new HashSet<>();

        for(Cookbook cookbook : cookbooks) {
            if(constraint == null || constraint.matches(cookbook.getSemanticVersion())) {
                filtered.add(cookbook);
            }
        }

        return orderCookbooks(filtered, numVersions);
    }

    public static Map<String, List<Cookbook>> filterWithConstraintMap(
                                                    Collection<Cookbook> cookbooks,
                                                    Map<String, VersionConstraint> constraintMap,
                                                    int numVersions) {

        Map<String, Set<Cookbook>> filtered = new HashMap<>();

        for (Cookbook cookbook : cookbooks) {
            final String cookbookName = cookbook.getName();
            final SemanticVersion cookbookVersion = cookbook.getSemanticVersion();
            final VersionConstraint constraint = constraintMap.get(cookbookName);

            if(!filtered.containsKey(cookbook.getCookbookName())) {
                filtered.put(cookbookName, new HashSet<Cookbook>());
            }

            // No constraint? match!
            // Constraint with no previously found thing? match!
            if(constraint == null || constraint.matches(cookbookVersion)) {
                filtered.get(cookbookName).add(cookbook);
            }
        }

        Map<String, List<Cookbook>> results = new HashMap<>();
        for(String cookbookName : filtered.keySet()) {
            results.put(cookbookName, CookbookFilter.orderCookbooks(filtered.get(cookbookName), numVersions));
        }

        return results;
    }

    public static Cookbook latestVersion(Collection<Cookbook> cookbooks) {
        List<Cookbook> latest = orderCookbooks(cookbooks, 1);
        if(latest.size() == 1) {
            return latest.get(0);
        } else {
            return null;
        }
    }

    public static List<Cookbook> orderCookbooks(Collection<Cookbook> cookbooks, final int numVersions) {
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
