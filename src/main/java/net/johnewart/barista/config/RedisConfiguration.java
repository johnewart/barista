package net.johnewart.barista.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RedisConfiguration {
    @JsonProperty
    private String host;

    @JsonProperty
    private Integer port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
