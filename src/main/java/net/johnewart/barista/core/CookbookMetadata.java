package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jewart
 * Date: 9/6/14
 * Time: 7:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CookbookMetadata {

    @JsonProperty("version")
    String version;
    @JsonProperty("name")
    String name;
    @JsonProperty("maintainer")
    String maintainer;
    @JsonProperty("maintainer_email")
    String maintainerEmail;
    @JsonProperty("description")
    String description;
    @JsonProperty("long_description")
    String longDescription;
    @JsonProperty("license")
    String license;
    @JsonProperty("attributes")
    Map<String, Object> attributes;
    @JsonProperty("recipes")
    Map<String, String> recipes;
    @JsonProperty("dependencies")
    Map<String, String> dependencies;

    public CookbookMetadata() { }
}
