package net.johnewart.barista.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.johnewart.barista.utils.ChefKeyGenerator;
import net.johnewart.barista.utils.PEMPair;

public class User {
    @JsonProperty("name")
    String name;

    @JsonProperty("username")
    String username;

    @JsonProperty("email")
    String email;

    @JsonProperty("admin")
    Boolean admin;

    @JsonProperty("public_key")
    String publicKey;

    @JsonProperty("password")
    String password;

    @JsonProperty("salt")
    String salt;

    @JsonProperty("private_key")
    String privateKey;

    public User() {
    }

    public User(String s) {
        username = s;
        name = s;
    }

    public User(User other) {
        this.username = other.username;
        this.name = other.name;
        this.publicKey = other.publicKey;
        this.admin = other.admin;
        this.email = other.email;
        this.password = other.password;
        this.salt = other.salt;
        this.privateKey = other.privateKey;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Boolean isAdmin() {
        if(admin == null) {
            return Boolean.FALSE;
        } else {
            return admin;
        }
    }

    public void update(User other) {
        if(other.privateKey != null)
            this.privateKey = other.privateKey;

        if(other.publicKey != null)
            this.publicKey = other.publicKey;

        if(other.password != null && !other.password.isEmpty())
            this.password = other.password;

        if(other.salt != null && !other.salt.isEmpty())
            this.salt = other.salt;

        if(other.admin != null)
            this.admin = other.admin;

        if(other.email != null)
            this.email = other.email;
    }


    public void generateKeys() {
        PEMPair pemPair = ChefKeyGenerator.generateKeyPairAsPEM();
        this.privateKey = new String(pemPair.privateKeyPEM);
        this.publicKey = new String(pemPair.publicKeyPEM);
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    @JsonProperty("public_key")
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
