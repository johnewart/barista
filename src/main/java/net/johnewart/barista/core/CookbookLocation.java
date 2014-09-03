package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class CookbookLocation {
    static String BASE_URL = "http://localhost:9090/cookbooks";

    @JsonProperty("url")
    final String url;

    @JsonProperty("versions")
    final Set<CookbookVersionLocation> cookbookVersionLocationSet;

    public CookbookLocation(Cookbook cookbook) {
        this(cookbook.getCookbookName());
    }

    public CookbookLocation(String cookbookName) {
        this.cookbookVersionLocationSet = new TreeSet<>();
        this.url =  String.format("%s/%s", BASE_URL, cookbookName);
    }

    public void addVersion(Cookbook cookbook) {
        this.cookbookVersionLocationSet.add(new CookbookVersionLocation(cookbook));
    }


    class CookbookVersionLocation implements Comparable<CookbookVersionLocation> {
        String url;
        SemanticVersion version;

        public CookbookVersionLocation(Cookbook cookbook) {
            this.url = String.format("%s/%s/%s", BASE_URL, cookbook.getCookbookName(), cookbook.getVersion());
            this.version = new SemanticVersion(cookbook.getVersion());
        }

        @JsonProperty("url")
        public String getUrl() {
            return url;
        }

        @JsonProperty("version")
        public String getVersionString() {
            return version.toString();
        }

        @Override
        public int compareTo(CookbookVersionLocation o) {
            return this.version.compareTo(o.version);
        }
    }

}
