package net.johnewart.barista.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RiakConfiguration {
    private List<String> hosts;
    private Integer port;

    @JsonProperty
    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    @JsonProperty
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
