package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.johnewart.barista.utils.ChefKeyGenerator;
import net.johnewart.barista.utils.PEMPair;
import org.bouncycastle.openssl.PEMWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Client {
    @JsonProperty("name")
    String name;

    @JsonProperty("node_name")
    String nodeName;

    @JsonProperty("json_class")
    String JSONClass = "Chef::ApiClient";

    @JsonProperty("chef_type")
    String chefType = "client";

    @JsonProperty("validator")
    Boolean validator;

    @JsonProperty("orgname")
    String orgName;

    @JsonProperty("public_key")
    String publicKey;

    @JsonProperty("admin")
    Boolean admin;

    @JsonProperty
    String certificate;

    @JsonProperty("private_key")
    String privateKey;

    public Client() {
      /*  privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEowIBAAKCAQEAtBr5Vq2X8sP12K7EP1SyWHaCglGdoUdiR37TQwRQ8l0NvpGq\n" +
                "GQNHqfxpZTwTAQAusuJo7BqNC+8A2X7weteLOQRB91yXKkwWVyucaDIReTSb4/iP\n" +
                "sWkQJqSM2gMVHmWCXYNgPIBta37hzq1TddSDzgCisyb/zzZpqxKUaC3nUlj/q4E/\n" +
                "9OL4HFNY+PyeSQI/FHBordc2vZxndSnBXXaztdf81TilYsRAUyPnmCzL5JKDrrv0\n" +
                "AObjXBFlYP1MQ/oenM1ETQhTmZM5pMlFLuqueN6RwBJV19nrmBWF1s0dnDHJmcJW\n" +
                "Z8YwjNuoVn+Z4irjFLsLkWDZXB9YMrwGe0qVpQIDAQABAoIBACA684PfbOG0z7oH\n" +
                "DHeI4KGUE6belCbYb2379DJ6A0JcBKmlx5rSS3DQCsxjA8OaFMFOYxhdaABKtQw2\n" +
                "+zIAgJmjeWqa1zJqi1TuKP88doQKNhuFz7Ef7BP+PnaFNrJJ2BqD+CTblrk165Lg\n" +
                "0Z60eSHUW0lUIVMjmaVTndtG266QkcwM/y71D8TLOzfNzqIUyijitqqfQsG80wVm\n" +
                "BxMlqpLjUutV9KnAnER11MfsVufao/jfTA8Wr+X64VBbDkrlSXqZmHgiBcxV4/e/\n" +
                "soZMaIib/5nb6pf2AEYa1mykcq0QtloRjPXTxtrgbV2O8Wji6OxFHYLwlkW0rzoR\n" +
                "YXn1/gECgYEAz/idsFyQilR3St9qOTJt4CJBQfAmn2iF6DDSfH58sG21hSwKgmRM\n" +
                "vFg8jDSa//ROPT6s7Lw/I1A3Xeq32BnvCvL3AYVQWYPGtYgh3DHzRCJtoAR8ebFm\n" +
                "Ib3UywVpWSHvwePTBPTnYQR7tDWwuitOdZXl3NzVGsQ1bNeLnm7iDGkCgYEA3bLq\n" +
                "TPoE7FfcDgicYfKIOqzAcu+AzRhT3HfVE20xPnetsPeRUtj7ZohN1azjMobCcgRr\n" +
                "H2bSYXjuecVH7ZK9cqlrrDc/4VMC/g00+zFgkKVFzq4QAAnNBEMia7lnRl2Lpa95\n" +
                "0RzwWHfNFr9yscqsioBFxs1iVPuPWySxYHd3B90CgYBqXf7Qx9cJWQAWZEQg8uDt\n" +
                "hLeZsOkgGMZ8JhRRpiPB3Kq9bPQHEqOIpRx6nSE1jc9CVb796Z3lQs6+kyDqPwFa\n" +
                "uT+KIJQi5FoKWJDw3P9NtsoY0JKVbx0MXtnp6F+kPc4xfYNdAqEgprlaRyeXYDTl\n" +
                "wP/qwWuhH/8vJuL66j1lGQKBgAxan/vSItwYuUZ+7Ff47+Z1IfRFrGPBa0rp0pHW\n" +
                "j9vvR1qJMSvws3GvPscdbzutjsBTxrRlQmv0FTXr7GostyngjwN1wLWJrq0Bh2ZI\n" +
                "Bh2JWC6APJwD78zBAPYHyt188P82nA8vEaWcZ21RFc8agCrnovvFDim2KvLlRI0f\n" +
                "mx5xAoGBAJdDRA8SxuoSP0TainOkvWcHWRxG1WFi7prez5GVJ0Cj+VhCfW2gmLGP\n" +
                "U8ZZq5bXtT4BWTUx/KO8anpjFq5jvqgnapXPgi7SHke89iXSV7xwjyP2sJDw08St\n" +
                "gXelbeFwOfeBs55eXEQYCyFs4lzDSdm+8FKxAnxOYG5AX/VcpR3D\n" +
                "-----END RSA PRIVATE KEY-----";*/

    }

    public Client(String s) {
        name = s;
        validator = true;
        orgName = "Chef metal \\m/";
        publicKey = "FOO";
        admin = true;
        certificate = "CERT";
    }

    public Client(Client other) {
        this.name = other.name;
        this.nodeName = other.nodeName;
        this.validator = other.validator;
        this.orgName = other.orgName;
        this.publicKey = other.publicKey;
        this.admin = other.admin;
        this.certificate = other.certificate;
    }

    public String getName() {
        return name;
    }

    public void update(Client other) {
        if(other.name != null)
            this.name = other.name;
        if(other.nodeName != null)
            this.nodeName = other.nodeName;
        if(other.orgName != null)
            this.orgName = other.orgName;
        if(other.privateKey != null && !other.privateKey.isEmpty())
            this.privateKey = other.privateKey;
        if(other.publicKey != null && !other.publicKey.isEmpty())
            this.publicKey = other.publicKey;
        if(other.admin != null)
            this.admin = other.admin;
        if(other.certificate != null)
            this.certificate = other.certificate;

        if(other.validator != null)
            this.validator = other.validator;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void generateKeys() {
        PEMPair pemPair = ChefKeyGenerator.generateKeyPairAsPEM();
        this.privateKey = new String(pemPair.privateKeyPEM);
        this.publicKey = new String(pemPair.publicKeyPEM);
        /*
        try {
            KeyPairGenerator generator;
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair keyPair = generator.genKeyPair();
            try {
                StringWriter stringWriter = new StringWriter();
                PEMWriter pemWriter = new PEMWriter(stringWriter);
                pemWriter.writeObject( keyPair.getPrivate());
                pemWriter.close();
                String privateKeyString = stringWriter.toString();
                this.privateKey = privateKeyString;

                stringWriter = new StringWriter();
                pemWriter = new PEMWriter(stringWriter);
                pemWriter.writeObject( keyPair.getPublic());
                pemWriter.close();
                String publicKeyString = stringWriter.toString();
                this.publicKey = publicKeyString;
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }*/
    }

    public boolean isAdmin() {
        return admin == null ? false : admin.booleanValue();
    }

    public boolean isValidator() {
        return validator == null ? false : validator.booleanValue();
    }
}
