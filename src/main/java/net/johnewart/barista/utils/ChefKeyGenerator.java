package net.johnewart.barista.utils;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class ChefKeyGenerator {
    private static final KeyPairGenerator generator = buildGenerator();

    private static KeyPairGenerator buildGenerator() {
        KeyPairGenerator g;
        try {
            g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            return g;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static KeyPair generateKeyPair() {
        if (generator != null) {
            return generator.generateKeyPair();
        } else {
            throw new IllegalStateException("Keypair Generator is null...");
        }
    }

    public static PEMPair generateKeyPairAsPEM() {
        try {
            return new PEMPair(ChefKeyGenerator.generateKeyPair());
        } catch (IOException e) {
            return new PEMPair();
        }
    }
}
