package net.johnewart.barista.utils;

import org.bouncycastle.openssl.PEMWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;

public class PEMPair {
    public final String publicKeyPEM, privateKeyPEM;

    public PEMPair() {
        this.publicKeyPEM = this.privateKeyPEM = "";
    }

    public PEMPair(KeyPair keyPair)  throws IOException {
        StringWriter stringWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(stringWriter);
        pemWriter.writeObject(keyPair.getPrivate());
        pemWriter.close();
        privateKeyPEM = stringWriter.toString();

        stringWriter = new StringWriter();
        pemWriter = new PEMWriter(stringWriter);
        pemWriter.writeObject( keyPair.getPublic());
        pemWriter.close();
        publicKeyPEM = stringWriter.toString();
    }
}
