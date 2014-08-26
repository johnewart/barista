package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.johnewart.barista.resources.SandboxResource;

import java.util.Map;

public class SandboxResponse {
    @JsonProperty("uri")
    public final String uri;

    @JsonProperty("sandbox_id")
    public final String sandboxId;

    @JsonProperty("checksums")
    public final Map checksums;

    public SandboxResponse(String uri, String sandboxId, Map<String, SandboxResource.FileStatus> checksums) {
        this.uri = uri;
        this.sandboxId = sandboxId;
        this.checksums = checksums;
    }
}
