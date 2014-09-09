package net.johnewart.barista.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.johnewart.barista.core.Cookbook;
import net.johnewart.barista.core.CookbookComponent;

import java.util.List;
import java.util.Map;

public class CookbookResponse {
    private final Cookbook cookbook;

    public CookbookResponse(Cookbook cookbook) {
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

    @JsonProperty("recipes")
    public List<CookbookComponent> getRecipes() {
        return cookbook.getRecipes();
    }

    @JsonProperty("frozen?")
    public boolean isFrozen() {
        return cookbook.isFrozen();
    }

    @JsonProperty("metadata")
    public Map getMetadata() {
        return cookbook.getMetadata();
    }

}
