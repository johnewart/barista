package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sandbox {
    private final static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

    String id;
    LocalDateTime creationTime;
    boolean completed;
    Map<String,String> checksums = new HashMap<>();


    public Sandbox() {
        this.id = UUID.randomUUID().toString();
        this.creationTime = LocalDateTime.now();
    }

    public Sandbox(Sandbox other) {
        this.checksums = other.checksums;
        this.completed = other.completed;
        this.creationTime = other.creationTime;
        this.id = other.id;
    }

    @JsonProperty("checksums")
    public Map<String, String> getChecksums() {
        return checksums;
    }

    @JsonProperty("guid")
    public String getId() {
        return id;
    }

    @JsonProperty("name")
    public String getName() {
        return id;
    }

    @JsonIgnore
    public void setName(String name) {
    }

    @JsonProperty("is_completed")
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @JsonProperty("create_time")
    public String getCreationTime() {
        String response = fmt.print(creationTime);
        return response;
    }

    public void setCreationTime(String timeString) {
        this.creationTime = LocalDateTime.parse(timeString);
    }
}
