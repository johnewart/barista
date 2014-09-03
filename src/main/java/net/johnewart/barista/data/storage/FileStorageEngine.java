package net.johnewart.barista.data.storage;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageEngine {
    public InputStream getResource(String resourceId);
    public boolean contains(String resourceId);
    void store(String fileChecksum, String stuff) throws IOException;
}
