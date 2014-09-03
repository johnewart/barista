package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Role {

    @JsonProperty("name")
    @Pattern(regexp = "^[\\w\\d-:]+$", message = "Field 'name' invalid")
    @NotNull(message = "Field 'name' missing")
    String name;

    @JsonProperty("chef_type")
    @Pattern(regexp = "^role$", message = "Field 'chef_type' invalid")
    String chefType = "role";

    @JsonProperty("json_class")
    @Pattern(regexp = "^Chef::Role$", message = "Field 'json_class' invalid")
    String jsonClass = "Chef::Role";

    @JsonProperty("run_list")
    RunList runList = new RunList();

    @JsonProperty("env_run_lists")
    Map<String, RunList> envRunLists = new HashMap<>();

    @JsonProperty("description")
    String description = "";

    @JsonProperty("default_attributes")
    Map<String, Object> defaultAttributes = new HashMap<>();

    @JsonProperty("override_attributes")
    Map<String, Object> overrideAttributes = new HashMap<>();

    public Role() { }

    public Role(Role other) {
        this.jsonClass = other.jsonClass;
        this.chefType = other.chefType;
        this.name = other.name;
        this.description = other.description;
        this.overrideAttributes = other.overrideAttributes;
        this.defaultAttributes = new HashMap<>();
        this.defaultAttributes = other.defaultAttributes;
        this.envRunLists = other.envRunLists;
        this.runList = other.runList;
    }

    public void update(Role other) {
        this.chefType = other.chefType;
        this.jsonClass = other.jsonClass;

        if(other.description == null) {
            this.description = "";
        } else {
            this.description = other.description;
        }

        if(other.overrideAttributes == null) {
            this.overrideAttributes = new HashMap<>();
        } else {
            this.overrideAttributes = other.overrideAttributes;
        }

        if(other.defaultAttributes == null) {
            this.defaultAttributes = new HashMap<>();
        } else {
            this.defaultAttributes = other.defaultAttributes;
        }

        if(other.envRunLists == null) {
            this.envRunLists = new HashMap<>();
        } else {
            this.envRunLists = other.envRunLists;
        }

        if(other.runList == null) {
            this.runList = new RunList();
        } else {
            this.runList = other.runList;
        }
    }

    public String getName() {
        return name;
    }

    public String getChefType() {
        return chefType;
    }

    public String getJsonClass() {
        return jsonClass;
    }

    public RunList getRunList() {
        return runList;
    }

    public Map<String, RunList> getEnvRunLists() {
        return envRunLists;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getDefaultAttributes() {
        return defaultAttributes;
    }

    public Map<String, Object> getOverrideAttributes() {
        return overrideAttributes;
    }

    public void clearEnvRunList(String environmentName) {
        if(this.envRunLists.get(environmentName) != null) {
            this.envRunLists.remove(environmentName);
        }
    }

    public void clearEnvRunLists() {
        this.envRunLists = new HashMap<>();
    }
}
