package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.*;

public class Node {
    private final Logger LOG = LoggerFactory.getLogger(Node.class);
    private final static String ROLE_PATTERN = "role\\[(\\w+)\\]";
    private final static String RECIPE_PATTERN = "\\w+(::\\w+)?(@(\\d+\\.*){2,3})?";
    private final static String FQ_RECIPE_PATTERN = String.format("recipe\\[%s\\]", RECIPE_PATTERN);

    @JsonProperty
    @Pattern(regexp = "^[\\w\\d-:]+$", message = "Field 'name' invalid")
    @NotNull(message = "Field 'name' missing")
    String name;

    @JsonProperty("chef_environment")
    @Pattern(regexp = "^[\\w\\d-]+$", message = "Field 'chef_environment' invalid")
    String chefEnvironment;

    @JsonProperty("run_list")
    List<String> runList;

    @JsonProperty("json_class")
    @Pattern(regexp = "^Chef::Node$", message = "Field 'json_class' invalid")
    String jsonClass = "Chef::Node";

    @JsonProperty("chef_type")
    @Pattern(regexp = "^node$", message = "Field 'chef_type' invalid")
    String chefType = "node";

    @JsonProperty
    Map automatic;

    @JsonProperty
    Map normal;

    @JsonProperty("default")
    Map def;

    @JsonProperty("override")
    Map override;

    public Node() { }

    public Node(Node other) {
        this.name = other.name;
        this.runList = other.runList;
        this.normal = other.normal;
        this.override = other.override;
        this.chefEnvironment = other.chefEnvironment;
        this.automatic = other.automatic;
        this.def = other.def;
    }

    public String getName() {
        return name;
    }

    public String getChefEnvironment() {
        return chefEnvironment;
    }

    public List<String> getRunList() {
        return runList;
    }

    public String getJsonClass() {
        return jsonClass;
    }

    public String getChefType() {
        return chefType;
    }

    public Map getAutomatic() {
        return automatic;
    }

    public Map getNormal() {
        return normal;
    }

    public Map getDef() {
        return def;
    }

    public Map getOverride() {
        return override;
    }

    public void normalizeRunList() {
        Set<String> entities = new HashSet<>();

        if(this.runList != null) {
            for(String entity : this.runList) {
                if (entity.matches(RECIPE_PATTERN)) {
                    entities.add("recipe[" + entity + "]");
                } else if (entity.matches(ROLE_PATTERN) || entity.matches(FQ_RECIPE_PATTERN)) {
                    entities.add(entity);
                }
            }
        }

        this.runList = new LinkedList<>(entities);
    }

    public static boolean isValidRunList(List<String> runList) {
        String recipe_match = String.format("(recipe\\[%s\\]|%s)", RECIPE_PATTERN, RECIPE_PATTERN);
        String patternmatch = String.format("^(%s|%s)$", ROLE_PATTERN, recipe_match);

        if(runList != null) {
            for(String s : runList) {
                if (!s.matches(patternmatch)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void updateFrom(Node other) {
        this.automatic = other.getAutomatic();
        this.chefType = other.getChefType();
        this.jsonClass = other.getJsonClass();
        this.chefEnvironment = other.getChefEnvironment();
        this.def = other.getDef();
        this.normal = other.getNormal();
        this.override = other.getOverride();

        this.runList = other.getRunList();
    }
}
