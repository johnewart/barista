package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;
import net.johnewart.barista.utils.URLGenerator;

import java.util.List;
import java.util.Map;

@JsonSnakeCase
public class Cookbook {
    String name;
    String cookbookName;
    String version;
    String chefType;
    String jsonClass;
    List<Map> definitions;
    List<Map> libraries;
    List<Map> attributes;
    List<Recipe> recipes;
    List<Map> providers;
    List<Map> resources;
    List<Map> templates;
    List<Map> rootFiles;
    List<Map> files;
    boolean frozen;
    Map metadata;
    SemanticVersion semanticVersion;

    public Cookbook() {
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("cookbook_name")
    public String getCookbookName() {
        return cookbookName;
    }

    public void setCookbookName(String cookbookName) {
        this.cookbookName = cookbookName;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("chef_type")
    public String getChefType() {
        return chefType;
    }

    public void setChefType(String chefType) {
        this.chefType = chefType;
    }

    @JsonProperty("json_class")
    public String getJsonClass() {
        return jsonClass;
    }

    public void setJsonClass(String jsonClass) {
        this.jsonClass = jsonClass;
    }

    @JsonIgnore
    public List<Map> getDefinitions() {
        return definitions;
    }

    @JsonProperty("definitions")
    public void setDefinitions(List<Map> definitions) {
        this.definitions = definitions;
    }

    @JsonIgnore
    public List<Map> getLibraries() {
        return libraries;
    }

    @JsonProperty("libraries")
    public void setLibraries(List<Map> libraries) {
        this.libraries = libraries;
    }

    @JsonIgnore
    public List<Map> getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(List<Map> attributes) {
        this.attributes = attributes;
    }

    @JsonProperty("recipes")
    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    @JsonIgnore
    public List<Map> getProviders() {
        return providers;
    }

    @JsonProperty("providers")
    public void setProviders(List<Map> providers) {
        this.providers = providers;
    }

    @JsonIgnore
    public List<Map> getTemplates() {
        return templates;
    }

    @JsonProperty("templates")
    public void setTemplates(List<Map> templates) {
        this.templates = templates;
    }

    @JsonIgnore
    public List<Map> getRootFiles() {
        return rootFiles;
    }

    @JsonProperty("root_files")
    public void setRootFiles(List<Map> rootFiles) {
        this.rootFiles = rootFiles;
    }

    @JsonIgnore
    public List<Map> getFiles() {
        return files;
    }

    @JsonProperty("files")
    public void setFiles(List<Map> files) {
        this.files = files;
    }

    @JsonProperty("frozen?")
    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    @JsonProperty("metadata")
    public Map getMetadata() {
        return metadata;
    }

    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("resources")
    public List<Map> getResources() {
        return resources;
    }

    public void setResources(List<Map> resources) {
        this.resources = resources;
    }

    @JsonIgnore
    public SemanticVersion getSemanticVersion() {
        if (semanticVersion == null) {
            semanticVersion = new SemanticVersion(version);
        }

        return semanticVersion;
    }

    public void updateFrom(Cookbook other) {
        this.definitions = other.definitions;
        this.libraries = other.libraries;
        this.attributes = other.attributes;
        this.recipes = other.recipes;
        this.templates = other.templates;
        this.rootFiles = other.rootFiles;
        this.files = other.files;
        this.metadata = other.metadata;
        this.resources = other.resources;
    }


}
