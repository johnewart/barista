package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;

public class Environment {
    @JsonProperty("name")
    @Pattern(regexp = "^[\\w\\d-:]+$", message = "Field 'name' invalid")
    @NotNull(message = "Field 'name' missing")
    String name;

    @JsonProperty("json_class")
    @Pattern(regexp = "^Chef::Environment", message = "Field 'json_class' invalid")
    String jsonClass;

    @JsonProperty("chef_type")
    @Pattern(regexp = "^environment", message = "Field 'chef_type' invalid")
    String chefType;

    @JsonProperty("description")
    String description;

    @JsonProperty("cookbook_versions")
    Map<String, String> cookbookVersions;

    @JsonProperty("default_attributes")
    Map<String, String> defaultAttributes;

    @JsonProperty("override_attributes")
    Map<String, String> overrideAttributes;

    public Environment() {
    }

    public void update(Environment other) {
        this.description = other.description;
        this.cookbookVersions = other.cookbookVersions;
        this.defaultAttributes = other.defaultAttributes;
        this.overrideAttributes = other.overrideAttributes;
    }

    public Environment(String s) {
        name = s;
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

    public Map<String, String> getDefaultAttributes() {
        return defaultAttributes;
    }

    public Map<String, String> getOverrideAttributes() {
        return overrideAttributes;
    }


}
