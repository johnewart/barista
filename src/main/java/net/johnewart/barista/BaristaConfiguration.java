package net.johnewart.barista;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import net.johnewart.barista.config.RedisConfiguration;
import net.johnewart.barista.config.RiakConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class BaristaConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty
    private RiakConfiguration riak;

    @JsonProperty
    private String storageEngine;

    @JsonProperty
    private RedisConfiguration redis;

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    public RiakConfiguration getRiakConfiguration() {
        return riak;
    }

    public String getStorageEngine() {
        return storageEngine;
    }

    public RedisConfiguration getRedisConfiguration() {
        return redis;
    }
}
