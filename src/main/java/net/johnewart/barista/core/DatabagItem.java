package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class DatabagItem {
    @JsonProperty("id")
    String id;
    @JsonProperty("chef_type")
    String chefType = "data_bag_item";
    @JsonProperty("json_class")
    String jsonClass = "Chef::DataBagItem";
    @JsonProperty("data_bag")
    String databagName;
    @JsonProperty("raw_data")
    Map<String, Object> data;

    public DatabagItem() { }

    public DatabagItem(String id, String databagName, Map<String, Object> rawData) {
        this.id = id;
        this.databagName = databagName;
        this.data = rawData;
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> toGetResponse() {
        Map<String, Object> result = new HashMap<>();
        result.putAll(data);
        result.put("id", id);
        return result;
    }

    public Map<String, Object> toPutResponse() {
        Map<String, Object> result = new HashMap<>();
        result.putAll(data);
        result.put("id", id);
        result.put("chef_type", chefType);
        result.put("data_bag", databagName);
        return result;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
