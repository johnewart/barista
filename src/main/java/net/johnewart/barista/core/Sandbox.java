package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.LocalTime;

import java.util.Map;
import java.util.UUID;

public class Sandbox {
    @JsonProperty("guid")
    String id;

    @JsonProperty("creation_time")
    LocalTime creationTime;

    @JsonProperty("is_completed")
    boolean completed;

    @JsonProperty("checksums")
    Map<String,String> checksums;


    public Sandbox() {
        this.id = UUID.randomUUID().toString();
    }

    public Sandbox(Sandbox other) {
        this.checksums = other.checksums;
        this.completed = other.completed;
        this.creationTime = other.creationTime;
        this.id = other.id;
    }

    public Map<String, String> getChecksums() {
        return checksums;
    }

    public String getId() {
        return id;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
