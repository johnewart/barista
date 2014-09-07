package net.johnewart.barista.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.Recipe;

import java.util.List;
import java.util.Map;

public class EnvironmentCookbookResponse {
    private final Cookbook cookbook;

    public EnvironmentCookbookResponse(Cookbook cookbook) {
        this.cookbook = cookbook;
    }

    @JsonProperty("name")
    public String getName() {
        return cookbook.getName();
    }

    @JsonProperty("cookbook_name")
    public String getCookbookName() {
        return cookbook.getCookbookName();
    }

    @JsonProperty("version")
    public String getVersion() {
        return cookbook.getVersion();
    }

    @JsonProperty("chef_type")
    public String getChefType() {
        return cookbook.getChefType();
    }

    @JsonProperty("json_class")
    public String getJsonClass() {
        return cookbook.getJsonClass();
    }

    @JsonProperty("definitions")
    public List<Map> getDefinitions() {
        return cookbook.getDefinitions();
    }

    @JsonProperty("libraries")
    public List<Map> getLibraries() {
        return cookbook.getLibraries();
    }

    @JsonProperty("attributes")
    public List<Map> getAttributes() {
        return cookbook.getAttributes();
    }

    @JsonProperty("recipes")
    public List<Recipe> getRecipes() {
        return cookbook.getRecipes();
    }

    @JsonProperty("providers")
    public List<Map> getProviders() {
        return cookbook.getProviders();
    }


    @JsonProperty("templates")
    public List<Map> getTemplates() {
        return cookbook.getTemplates();
    }


    @JsonProperty("root_files")
    public List<Map> getRootFiles() {
        return cookbook.getRootFiles();
    }


    @JsonProperty("files")
    public List<Map> getFiles() {
        return cookbook.getFiles();
    }


    @JsonProperty("frozen?")
    public boolean isFrozen() {
        return cookbook.isFrozen();
    }


    @JsonProperty("metadata")
    public Map getMetadata() {
        return cookbook.getMetadata();
    }


    @JsonProperty("resources")
    public List<Map> getResources() {
        return cookbook.getResources();
    }


}
