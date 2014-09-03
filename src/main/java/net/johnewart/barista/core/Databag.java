package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Databag {
    @JsonProperty("name")
    String name;

    @JsonProperty("items")
    Map<String, DatabagItem> items = new HashMap<>();

    @JsonProperty("chef_type")
    String chefType = "data_bag";

    @JsonProperty("json_class")
    String jsonClass = "Chef::DataBag";

    public Databag() { }

    public Databag(Databag other) {
        this.name = other.name;
        this.items = other.items;
    }

    public Databag(String databagName) {
        this.name = databagName;
        this.items = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, DatabagItem> getItems() {
        return items;
    }

    public Map<String, Object> toResponseMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("json_class", jsonClass);
        result.put("chef_type", chefType);
        return result;
    }
}
