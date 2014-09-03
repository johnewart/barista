package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    @JsonProperty("name")
    @Pattern(regexp = "^[A-Za-z0-9:_-]+$", message = "Field 'name' invalid")
    @NotNull(message = "Field 'name' missing")
    String name;

    @JsonProperty("json_class")
    @Pattern(regexp = "^Chef::Environment", message = "Field 'json_class' invalid")
    @NotNull(message = "Field 'json_class' missing")
    String jsonClass = "Chef::Environment";

    @JsonProperty("chef_type")
    @Pattern(regexp = "^environment$", message = "Field 'chef_type' invalid")
    @NotNull(message = "Field 'chef_type' missing")
    String chefType = "environment";

    @JsonProperty("description")
    //@Pattern(regexp = "^[A-Za-z_][\\w\\s]*$", message = "Field 'description' invalid")
    //@NotNull(message = "Field 'description' missing")
    String description = "";

    @JsonProperty("cookbook_versions")
    Map<String, String> cookbookVersions = new HashMap<>();

    @JsonProperty("default_attributes")
    Map<String, Object> defaultAttributes = new HashMap<>();

    @JsonProperty("override_attributes")
    Map<String, Object> overrideAttributes = new HashMap<>();

    public Environment() {  }

    public Environment(String name) {
        this.name = name;

        if(name.equals("_default")) {
            this.description = "The default Chef environment";
        } else {
            this.description = "";
        }

        this.cookbookVersions = new HashMap<>();
        this.defaultAttributes = new HashMap<>();
        this.overrideAttributes = new HashMap<>();
    }

    public Environment(Environment other) {
       this.name = other.name;
        this.description = other.description;
        this.cookbookVersions = other.cookbookVersions;
        this.defaultAttributes = other.defaultAttributes;
        this.overrideAttributes = other.overrideAttributes;
        this.chefType = other.chefType;
        this.jsonClass = other.jsonClass;
    }

    public void update(Environment other) {
        if(other.description == null) {
            this.description = "";
        } else {
            this.description = other.description;
        }

        if(other.cookbookVersions == null) {
            this.cookbookVersions = new HashMap<>();
        } else {
            this.cookbookVersions = other.cookbookVersions;
        }

        if(other.defaultAttributes == null) {
            this.defaultAttributes = new HashMap<>();
        } else {
            this.defaultAttributes = other.defaultAttributes;
        }

        if(other.overrideAttributes == null) {
            this.overrideAttributes = new HashMap<>();
        } else {
            this.overrideAttributes = other.overrideAttributes;
        }
    }

    public String getName() {
        return name;
    }

    public String getJsonClass() {
        return jsonClass;
    }

    public String getChefType() {
        return chefType;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getCookbookVersions() {
        return cookbookVersions;
    }

    public Map<String, Object> getDefaultAttributes() {
        return defaultAttributes;
    }

    public Map<String, Object> getOverrideAttributes() {
        return overrideAttributes;
    }

    @JsonIgnore
    public Map<String, VersionConstraint> getVersionConstraints() {
        Map<String, VersionConstraint> results = new HashMap<>();

        for(String cookbookName : cookbookVersions.keySet()) {
            results.put(cookbookName, new VersionConstraint(cookbookVersions.get(cookbookName)));
        }

        return results;
    }


    public void setJsonClass(String jsonClass) {
        this.jsonClass = jsonClass;
    }

    public String toString() {
        return this.getName();
    }
}
