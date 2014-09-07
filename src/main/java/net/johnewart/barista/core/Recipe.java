package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.johnewart.barista.utils.URLGenerator;

public class Recipe {

    @JsonProperty("name")
    String name;
    @JsonProperty("path")
    String path;
    @JsonProperty("checksum")
    String checksum;
    @JsonProperty("specificity")
    String specificity;

    public Recipe() { }

    @JsonProperty("url")
    public String getUrl() {
        return URLGenerator.generateUrl("file_store/" + this.checksum);
    }

    public String getName() {
        return name;
    }

    public String getChecksum() {
        return checksum;
    }
}
