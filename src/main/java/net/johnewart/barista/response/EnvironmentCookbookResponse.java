package net.johnewart.barista.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.CookbookResource;

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
    public List<CookbookResource> getDefinitions() {
        return cookbook.getDefinitions();
    }

    @JsonProperty("libraries")
    public List<CookbookResource> getLibraries() {
        return cookbook.getLibraries();
    }

    @JsonProperty("attributes")
    public List<CookbookResource> getAttributes() {
        return cookbook.getAttributes();
    }

    @JsonProperty("recipes")
    public List<CookbookResource> getRecipes() {
        return cookbook.getRecipes();
    }

    @JsonProperty("providers")
    public List<CookbookResource> getProviders() {
        return cookbook.getProviders();
    }


    @JsonProperty("templates")
    public List<CookbookResource> getTemplates() {
        return cookbook.getTemplates();
    }


    @JsonProperty("root_files")
    public List<CookbookResource> getRootFiles() {
        return cookbook.getRootFiles();
    }


    @JsonProperty("files")
    public List<CookbookResource> getFiles() {
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
    public List<CookbookResource> getResources() {
        return cookbook.getResources();
    }


}
