package org.white5moke.handoff.document;

import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class EncryptionDocument {
    public static final String KEY_PAIR_ALGORITHM = "RSA";
    public static final int KEY_SIZE = 4096;

    private KeyPair keyPair;

    private static final String JSON_PRIV_KEY = "priv";
    private static final String JSON_PUB_KEY = "pub";

    public EncryptionDocument() {
        try {
            generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
        gen.initialize(KEY_SIZE);

        keyPair = gen.generateKeyPair();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        try {
            j.put(JSON_PRIV_KEY, KeyDocument.ez(keyPair.getPrivate().getEncoded()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            j.put(JSON_PUB_KEY, KeyDocument.ez(keyPair.getPublic().getEncoded()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return j;
    }
}
