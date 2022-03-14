package org.white5moke.handoff.doc;

import org.json.JSONObject;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SigningDocument {
    private KeyPair keyPair;

    public SigningDocument() {}

    public void generate(int keySize) {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
            SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
            gen.initialize(keySize, rand);
            setKeyPair(gen.generateKeyPair());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    @Override
    public String toString() {
        return toJson().toString(4);
    }

    public JSONObject toJson() {
        JSONObject j = new JSONObject();

        j.put("priv", Base64.getEncoder().encodeToString(getKeyPair().getPrivate().getEncoded()));
        j.put("pub", Base64.getEncoder().encodeToString(getKeyPair().getPublic().getEncoded()));

        return j;
    }
}
