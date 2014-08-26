package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.List;
import java.util.Map;

@JsonSnakeCase
public class Cookbook {
    @JsonProperty
    String name;
    @JsonProperty
    String cookbookName;
    @JsonProperty
    String version;
    @JsonProperty
    String chefType;
    @JsonProperty
    String jsonClass;
    @JsonProperty
    List<Map> definitions;
    @JsonProperty
    List<Map> libraries;
    @JsonProperty
    List<Map> attributes;
    @JsonProperty
    List<Map> recipes;
    @JsonProperty
    List<Map> providers;
    @JsonProperty
    List<Map> templates;
    @JsonProperty
    List<Map> rootFiles;
    @JsonProperty
    List<Map> files;
    @JsonProperty("frozen?")
    boolean frozen;
    @JsonProperty
    Map metadata;

    @JsonIgnore
    SemanticVersion semanticVersion;

    public Cookbook() { }

    public String getName() {
        return name;
    }

    public String getCookbookName() {
        return cookbookName;
    }

    public String getVersion() {
        return version;
    }

    public String getChefType() {
        return chefType;
    }

    public String getJsonClass() {
        return jsonClass;
    }

    public List<Map> getDefinitions() {
        return definitions;
    }

    public List<Map> getLibraries() {
        return libraries;
    }

    public List<Map> getAttributes() {
        return attributes;
    }

    public List<Map> getRecipes() {
        return recipes;
    }

    public List<Map> getProviders() {
        return providers;
    }

    public List<Map> getTemplates() {
        return templates;
    }

    public List<Map> getRootFiles() {
        return rootFiles;
    }

    public List<Map> getFiles() {
        return files;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public Map getMetadata() {
        return metadata;
    }

    public SemanticVersion getSemanticVersion() {
        if (semanticVersion == null) {
            semanticVersion = new SemanticVersion(version);
        }

        return semanticVersion;
    }
}
