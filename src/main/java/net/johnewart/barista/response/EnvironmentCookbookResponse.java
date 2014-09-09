package net.johnewart.barista.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.CookbookComponent;

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
    public List<CookbookComponent> getDefinitions() {
        return cookbook.getDefinitions();
    }

    @JsonProperty("libraries")
    public List<CookbookComponent> getLibraries() {
        return cookbook.getLibraries();
    }

    @JsonProperty("attributes")
    public List<CookbookComponent> getAttributes() {
        return cookbook.getAttributes();
    }

    @JsonProperty("recipes")
    public List<CookbookComponent> getRecipes() {
        return cookbook.getRecipes();
    }

    @JsonProperty("providers")
    public List<CookbookComponent> getProviders() {
        return cookbook.getProviders();
    }


    @JsonProperty("templates")
    public List<CookbookComponent> getTemplates() {
        return cookbook.getTemplates();
    }


    @JsonProperty("root_files")
    public List<CookbookComponent> getRootFiles() {
        return cookbook.getRootFiles();
    }


    @JsonProperty("files")
    public List<CookbookComponent> getFiles() {
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
    public List<CookbookComponent> getResources() {
        return cookbook.getResources();
    }


}
