package net.johnewart.barista.data.storage;

import java.io.*;

public class OnDiskFileStorageEngine implements FileStorageEngine {
    private final String storagePath;

    public OnDiskFileStorageEngine(String storagePath) {
        this.storagePath = storagePath;
        File f = new File(storagePath);
        if(!f.exists() ) {
            f.mkdirs();
        }
    }

    @Override
    public InputStream getResource(String resourceId) {
        File f = new File(storagePath + "/" + resourceId);
        try {
            return new FileInputStream(f);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public boolean contains(String resourceId) {
        File f = new File(storagePath + "/" + resourceId);
        return f.exists();
    }

    @Override
    public void store(String resourceId, String data) throws IOException {
        File f = new File(storagePath + "/" + resourceId);
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f));
        os.write(data.getBytes());
        os.close();
    }
}
