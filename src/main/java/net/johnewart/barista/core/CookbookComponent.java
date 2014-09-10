package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.johnewart.barista.utils.URLGenerator;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CookbookComponent {

    @JsonProperty("name")
    String name;
    @JsonProperty("path")
    String path;
    @JsonProperty("checksum")
    String checksum;
    @JsonProperty("specificity")
    String specificity;

    public CookbookComponent() { }

    @JsonProperty("url")
    public String getUrl() {
        if(this.checksum != null)
            return URLGenerator.generateUrl("file_store/" + this.checksum);
        else
            return null;
    }

    @JsonIgnore
    public void setUrl(String url) {
    }

    public String getName() {
        return name;
    }

    public String getChecksum() {
        return checksum;
    }
}
