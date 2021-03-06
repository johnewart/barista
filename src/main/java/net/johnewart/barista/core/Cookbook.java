package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@JsonSnakeCase
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Cookbook {
    String name;
    String cookbookName;
    String version;
    String chefType;
    String jsonClass;
    Boolean frozen;
    
    List<CookbookComponent> definitions;
    List<CookbookComponent> libraries;
    List<CookbookComponent> attributes;
    List<CookbookComponent> recipes;
    List<CookbookComponent> providers;
    List<CookbookComponent> resources;
    List<CookbookComponent> templates;
    List<CookbookComponent> rootFiles;
    List<CookbookComponent> files;
    Map<String, Object> metadata;
    
    SemanticVersion semanticVersion;

    public Cookbook() {
    }

    // TODO: Apache commons serialization utils instead?
    public Cookbook(Cookbook other) {
        this.name = other.name;
        this.cookbookName = other.cookbookName;
        this.version = other.version;
        this.chefType = other.chefType;
        this.jsonClass = other.jsonClass;
        this.frozen = other.frozen;

        // TODO: Seriously? Our client really cares if null data comes back as empty hashes / arrays? WTF?
        if(other.definitions != null) 
            this.definitions = new LinkedList<>(other.definitions);
        
        if(other.libraries != null) 
            this.libraries = new LinkedList<>(other.libraries);

        if(other.attributes != null) 
            this.attributes = new LinkedList<>(other.attributes);

        if(other.recipes != null) 
            this.recipes = new LinkedList<>(other.recipes);

        if(other.providers != null) 
            this.providers = new LinkedList<>(other.providers);

        if(other.resources != null)
            this.resources = new LinkedList<>(other.resources);

        if(other.templates != null)
            this.templates = new LinkedList<>(other.templates);

        if(other.rootFiles != null)
            this.rootFiles = new LinkedList<>(other.rootFiles);

        if(other.files != null)
            this.files = new LinkedList<>(other.files);

        if(other.metadata != null)
            this.metadata = new HashMap<>(other.metadata);
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

    @JsonProperty("definitions")
    public List<CookbookComponent> getDefinitions() {
        return definitions;
    }

    @JsonProperty("definitions")
    public void setDefinitions(List<CookbookComponent> definitions) {
        this.definitions = definitions;
    }

    @JsonProperty("libraries")
    public List<CookbookComponent> getLibraries() {
        return libraries;
    }

    @JsonProperty("libraries")
    public void setLibraries(List<CookbookComponent> libraries) {
        this.libraries = libraries;
    }

    @JsonProperty("attributes")
    public List<CookbookComponent> getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(List<CookbookComponent> attributes) {
        this.attributes = attributes;
    }

    @JsonProperty("recipes")
    public List<CookbookComponent> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<CookbookComponent> recipes) {
        this.recipes = recipes;
    }

    @JsonProperty("providers")
    public List<CookbookComponent> getProviders() {
        return providers;
    }

    @JsonProperty("providers")
    public void setProviders(List<CookbookComponent> providers) {
        this.providers = providers;
    }

    @JsonProperty("templates")
    public List<CookbookComponent> getTemplates() {
        return templates;
    }

    @JsonProperty("templates")
    public void setTemplates(List<CookbookComponent> templates) {
        this.templates = templates;
    }

    @JsonProperty("root_files")
    public List<CookbookComponent> getRootFiles() {
        return rootFiles;
    }

    @JsonProperty("root_files")
    public void setRootFiles(List<CookbookComponent> rootFiles) {
        this.rootFiles = rootFiles;
    }

    @JsonProperty("files")
    public List<CookbookComponent> getFiles() {
        return files;
    }

    @JsonProperty("files")
    public void setFiles(List<CookbookComponent> files) {
        this.files = files;
    }

    @JsonProperty("frozen?")
    public boolean isFrozen() {
        if(frozen == null) {
            return false;
        } else {
            return frozen.booleanValue();
        }
    }

    public void setFrozen(Boolean frozen) {
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
    public List<CookbookComponent> getResources() {
        return resources;
    }

    public void setResources(List<CookbookComponent> resources) {
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
        if(other.definitions != null)
            this.definitions = other.definitions;
        if(other.libraries != null)
            this.libraries = other.libraries;
        if(other.attributes != null)
            this.attributes = other.attributes;
        if (other.recipes != null)
            this.recipes = other.recipes;
        if(other.templates != null)
            this.templates = other.templates;
        if(other.version != null)
            this.version = other.version;
        if(other.rootFiles != null)
            this.rootFiles = other.rootFiles;
        if(other.files != null)
            this.files = other.files;
        if(other.metadata != null)
            this.metadata = other.metadata;
        if(other.resources != null)
            this.resources = other.resources;
        if(other.frozen != null)
            this.frozen = other.frozen;
    }

    @JsonIgnore
    public Map<String, VersionConstraint> getDependencies() {
        Map<String, String> dependencies = (Map<String, String>) getMetadata().get("dependencies");
        Map<String, VersionConstraint> results = new HashMap<>();
        if(dependencies != null) {
            for(String depCookbookName : dependencies.keySet()) {
                VersionConstraint vc = new VersionConstraint(dependencies.get(depCookbookName));
                results.put(depCookbookName, vc);
            }
        }

        return results;
    }


}
