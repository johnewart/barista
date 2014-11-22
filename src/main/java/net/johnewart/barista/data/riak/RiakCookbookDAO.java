package net.johnewart.barista.data.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.datatypes.*;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.crdt.types.RiakMap;
import com.basho.riak.client.core.query.crdt.types.RiakSet;
import com.basho.riak.client.core.util.BinaryValue;
import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.SemanticVersion;
import net.johnewart.barista.core.VersionConstraint;
import net.johnewart.barista.core.cookbook.CookbookFilter;
import net.johnewart.barista.data.CookbookDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RiakCookbookDAO implements CookbookDAO {
    private final RiakCluster cluster;
    private static final Logger LOG = LoggerFactory.getLogger(RiakCookbookDAO.class);
    private static final String VERSIONS_KEY = "_versions";
    private static final String COOKBOOK_DATA_TYPE = "cookbooks";
    private static final Location COOKBOOK_NAMES_LOCATION = new Location(new Namespace("sets", "cookbooks"), "names");

    public RiakCookbookDAO(RiakCluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public List<Cookbook> findAll() {
        List<Cookbook> cookbooks = new LinkedList<>();
        try {
            for(String cookbookName : getAllCookbookNames()) {
                for (String cookbookVersion : getCookbookVersions(cookbookName)) {
                    Cookbook cookbook = findByNameAndVersion(cookbookName, cookbookVersion);
                    if(cookbook != null)
                        cookbooks.add(cookbook);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return cookbooks;
    }

    @Override
    public Set<String> findAllCookbookNames() {
        return getAllCookbookNames();
    }

    @Override
    public void store(Cookbook cookbook) {
        try {
            Namespace cookbookBucket = new Namespace("cookbooks", cookbook.getCookbookName());
            Location cookbookLocation = new Location(cookbookBucket, cookbook.getVersion());
            StoreValue storeCookbookOp = new StoreValue.Builder(cookbook)
                    .withLocation(cookbookLocation)
                    .build();

            RiakClient riakClient = new RiakClient(cluster);
            riakClient.execute(storeCookbookOp);

            addCookbookVersion(cookbook.getCookbookName(), cookbook.getVersion());
            addCookbookName(cookbook.getCookbookName());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void removeAll() {
        for(Cookbook cookbook : findAll()) {
            removeByNameAndVersion(cookbook.getCookbookName(), cookbook.getVersion());
        }
    }

    @Override
    // TODO: Use 2i for this? this is not going to be fast.
    public Set<Cookbook> findAllByName(String cookbookName) {
        Set<Cookbook> cookbooks = new HashSet<>();
        try {
            Set<String> cookbookVersions = getCookbookVersions(cookbookName);

            for (String version : cookbookVersions)
            {
                Cookbook cookbook = findByNameAndVersion(cookbookName, version);
                if (cookbook != null)
                    cookbooks.add(cookbook);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return cookbooks;
    }

    @Override
    public Cookbook findByNameAndVersion(String cookbookName, String version) {
        if (version == null || cookbookName == null) {
            return null;
        }

        try {
            Location cookbookLocation = new Location(new Namespace(COOKBOOK_DATA_TYPE, cookbookName), version);

            RiakClient riakClient = new RiakClient(cluster);

            FetchValue fetchCookbookOp = new FetchValue.Builder(cookbookLocation)
                    .build();
            Cookbook fetchedCookbook = riakClient.execute(fetchCookbookOp).getValue(Cookbook.class);

            return fetchedCookbook;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public Cookbook findLatestVersion(String cookbookName) {
        SemanticVersion latestVersion = getLatestVersionOfCookbook(cookbookName);
        if (latestVersion != null) {
            return findByNameAndVersion(cookbookName, latestVersion.toString());
        } else {
            return null;
        }
    }

    @Override
    public Set<Cookbook> findWithDependencies(String cookbookName, String version) {
        Cookbook root = findByNameAndVersion(cookbookName, version);
        Set<Cookbook> cookbooks = new HashSet<>();
        cookbooks.add(root);

        for(String depCookbookName : root.getDependencies().keySet()) {
            VersionConstraint constraint = root.getDependencies().get(depCookbookName);
            Cookbook bestMatch = findOneWithConstraints(depCookbookName, constraint, 1).get(0);
            cookbooks.addAll(findWithDependencies(depCookbookName, bestMatch.getVersion()));
        }

        return cookbooks;
    }

    @Override
    public Set<Cookbook> removeByName(String cookbookName) {
        Set<Cookbook> removed = new HashSet<>();
        for(String cookbookVersion : getCookbookVersions(cookbookName)) {
            removed.add(removeByNameAndVersion(cookbookName, cookbookVersion));
        }
        removeCookbookName(cookbookName);
        return removed;
    }

    @Override
    public Cookbook removeByNameAndVersion(String cookbookName, String cookbookVersion) {

        Cookbook cookbook = findByNameAndVersion(cookbookName, cookbookVersion);
        Location cookbookLocation = new Location(new Namespace(COOKBOOK_DATA_TYPE, cookbookName), cookbookVersion);
        DeleteValue delete = new DeleteValue.Builder(cookbookLocation).build();

        RiakClient riakClient = new RiakClient(cluster);
        try {
            riakClient.execute(delete);
            removeCookbookVersion(cookbookName, cookbookVersion);
        } catch (Exception e) {
            LOG.error("Error removing cookbook " + cookbookName + "@"  + cookbookVersion + ": ", e);
        }

        return cookbook;
    }

    @Override
    public Map<String, List<Cookbook>> findAllWithConstraints(Map<String, VersionConstraint> constraintMap, int numVersions) {
        return CookbookFilter.filterWithConstraintMap(findAll(), constraintMap, numVersions);
    }

    @Override
    public List<Cookbook> findOneWithConstraints(String cookbookName, VersionConstraint constraint, int numVersions) {
        Set<Cookbook> cookbooks = findAllByName(cookbookName);
        List<Cookbook> results = CookbookFilter.filterCookbooks(cookbooks, constraint, numVersions);
        return results;
    }

    @Override
    public List<Cookbook> findLatestVersions() {
        List<Cookbook> latestCookbooks = new LinkedList<>();
        for(String cookbookName : getAllCookbookNames()) {
            String latestVersion = getLatestVersionOfCookbook(cookbookName).toString();
            Cookbook cookbook = findByNameAndVersion(cookbookName, latestVersion);
            if(cookbook != null) {
                latestCookbooks.add(cookbook);
            }
        }
        return latestCookbooks;
    }


    private void addCookbookName(String cookbookName) {
        SetUpdate su = new SetUpdate().add(BinaryValue.create(cookbookName));
        UpdateSet update = new UpdateSet.Builder(COOKBOOK_NAMES_LOCATION, su).build();
        RiakClient riakClient = new RiakClient(cluster);

        try {
            riakClient.execute(update);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeCookbookName(String cookbookName) {

        RiakClient riakClient = new RiakClient(cluster);
        FetchSet fetch = new FetchSet.Builder(COOKBOOK_NAMES_LOCATION).build();

        try {
            Context ctx = riakClient.execute(fetch).getContext();

            SetUpdate su = new SetUpdate().remove(BinaryValue.create(cookbookName));
            UpdateSet update = new UpdateSet.Builder(COOKBOOK_NAMES_LOCATION, su)
                    .withContext(ctx)
                    .build();

            riakClient.execute(update);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  Set<String> getAllCookbookNames() {
        FetchSet fetch = new FetchSet.Builder(COOKBOOK_NAMES_LOCATION).build();
        RiakClient riakClient = new RiakClient(cluster);
        Set<String> cookbookNames = new HashSet<>();
        try {
            FetchSet.Response response = riakClient.execute(fetch);
            RiakSet cookbookNameSet = response.getDatatype();
            for(BinaryValue cookbookName : cookbookNameSet.view()) {
                cookbookNames.add(cookbookName.toString());
            }
        } catch (Exception e) {
            LOG.error("Unable to fetch cookbook names: ", e);
        }

        return cookbookNames;
    }

    private Set<String> getCookbookVersions(String cookbookName) {
        Location versionsMap = getVersionsMap(cookbookName);
        FetchMap fetch = new FetchMap.Builder(versionsMap).build();
        RiakClient riakClient = new RiakClient(cluster);
        Set<String> cookbookVersions = new HashSet<>();
        try {
            FetchMap.Response response = riakClient.execute(fetch);
            RiakMap versionMap = response.getDatatype();
            if(versionMap != null) {
                try {
                    RiakSet versions = versionMap.getSet("versions");
                    for(BinaryValue version : versions.view()) {
                        cookbookVersions.add(version.toString());
                    }
                } catch (NullPointerException npe) {
                    // No such set
                    LOG.warn("No versions set!");
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to fetch cookbook names: ", e);
        }

        return cookbookVersions;
    }

    private void addCookbookVersion(String cookbookName, String version) {
        Location versionsMap = getVersionsMap(cookbookName);

        SemanticVersion possibleUpdate = new SemanticVersion(version);
        SemanticVersion currentLatest = getLatestVersionOfCookbook(cookbookName);

        if (currentLatest == null || possibleUpdate.isGreaterThan(currentLatest)) {
            LOG.debug("Updating latest version of " + cookbookName + " to " + version);
            setLatestVersionOfCookbook(cookbookName, possibleUpdate);
        }


        SetUpdate su = new SetUpdate()
                .add(BinaryValue.create(version));
        MapUpdate mu = new MapUpdate()
                .update("versions", su);
        UpdateMap update = new UpdateMap.Builder(versionsMap, mu)
                .build();
        RiakClient riakClient = new RiakClient(cluster);

        try {
            riakClient.execute(update);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void removeCookbookVersion(String cookbookName, String version) {
        Location versionsMap = getVersionsMap(cookbookName);

        SemanticVersion versionToRemove = new SemanticVersion(version);
        SemanticVersion currentLatest = getLatestVersionOfCookbook(cookbookName);

        if(currentLatest != null) {
            if(versionToRemove.equals(currentLatest)) {
                Set<String> versionStrings = getCookbookVersions(cookbookName);
                List<SemanticVersion> versionList = new ArrayList<>(versionStrings.size());
                for(String versionString : versionStrings) {
                    SemanticVersion sv = new SemanticVersion(versionString);
                    if (!sv.equals(versionToRemove)) {
                        versionList.add(sv);
                    }
                }
                Collections.sort(versionList);
                if(versionList.size() > 0) {
                    SemanticVersion newLatestVersion = versionList.get(0);
                    setLatestVersionOfCookbook(cookbookName, newLatestVersion);
                } else {
                    // No more versions of this thing, remove it
                    removeCookbookName(cookbookName);
                    setLatestVersionOfCookbook(cookbookName, null);
                }
            }
        }

        FetchMap fetch = new FetchMap.Builder(versionsMap).build();
        RiakClient riakClient = new RiakClient(cluster);

        try {
            Context ctx = riakClient.execute(fetch).getContext();
            SetUpdate su = new SetUpdate()
                    .remove(BinaryValue.create(version));
            MapUpdate mu = new MapUpdate()
                    .update("versions", su);
            UpdateMap update = new UpdateMap.Builder(versionsMap, mu)
                    .withContext(ctx)
                    .build();
            riakClient.execute(update);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setLatestVersionOfCookbook(String cookbookName, SemanticVersion latestVersion) {
        Location versionsMap = getVersionsMap(cookbookName);
        final String versionString;
        if(latestVersion != null) {
            versionString = latestVersion.toString();
        } else {
            versionString = "";
        }

        RegisterUpdate vu = new RegisterUpdate(BinaryValue.create(versionString));
        MapUpdate versionUpdate = new MapUpdate()
                .update("latest_version", vu);
        UpdateMap update = new UpdateMap.Builder(versionsMap, versionUpdate).build();
        RiakClient riakClient = new RiakClient(cluster);

        try {
            riakClient.execute(update);
        } catch (Exception e) {
            LOG.error("Problem setting latest version of " + cookbookName + " to " + latestVersion + ": ", e);
        }
    }

    private SemanticVersion getLatestVersionOfCookbook(String cookbookName) {
        try {
            Location versionsMap = getVersionsMap(cookbookName);
            FetchMap fetch = new FetchMap.Builder(versionsMap).build();
            RiakClient riakClient = new RiakClient(cluster);
            FetchMap.Response response = riakClient.execute(fetch);
            try {
                String latestVersion = response.getDatatype()
                    .getRegister("latest_version")
                    .view()
                    .toString();
                return new SemanticVersion(latestVersion);
            } catch (NullPointerException npe) {
                // That thing didn't exist!
                return null;
            }
        } catch (Exception e) {
            LOG.error("Problem getting latest version of " + cookbookName + ": ", e);
            return null;
        }
    }

    private Location getVersionsMap(String cookbookName) {
        return new Location(new Namespace("maps", "cookbook_versions"), cookbookName);
    }





}
