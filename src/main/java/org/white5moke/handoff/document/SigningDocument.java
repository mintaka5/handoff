package org.white5moke.handoff.document;

import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SigningDocument {
    public static final int KEY_SIZE = 256;
    public static final String KEY_SIGNING_ALGORITHM = "SHA256withECDSA";
    public static final String KEY_PAIR_ALGORITHM = "EC";
    public static final String RANDOM_ALGORITHM = "SHA1PRNG";
    private static final String JSON_PRIV_KEY = "priv";
    private static final String JSON_PUB_KEY = "pub";

    private KeyPair keyPair;

    public SigningDocument() {
        try {
            generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator g = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
        SecureRandom r = SecureRandom.getInstance(RANDOM_ALGORITHM);
        g.initialize(KEY_SIZE, r);

        keyPair = g.generateKeyPair();
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
