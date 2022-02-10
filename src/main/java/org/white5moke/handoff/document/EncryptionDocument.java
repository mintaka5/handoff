package org.white5moke.handoff.document;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.zip.DataFormatException;

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

    public EncryptionDocument(JSONObject j) {
        try {
            byte[] privBs = KeyDocument.notEz(j.getString(JSON_PRIV_KEY).trim());
            byte[] pubBs = KeyDocument.notEz(j.getString(JSON_PUB_KEY).trim());

            PKCS8EncodedKeySpec spec1 = new PKCS8EncodedKeySpec(privBs);
            KeyFactory fac1 = KeyFactory.getInstance(KEY_PAIR_ALGORITHM);
            PrivateKey privKey = fac1.generatePrivate(spec1);

            X509EncodedKeySpec spec2 = new X509EncodedKeySpec(pubBs);
            PublicKey pubKey = fac1.generatePublic(spec2);

            keyPair = new KeyPair(pubKey, privKey);
        } catch (DataFormatException | NoSuchAlgorithmException | InvalidKeySpecException e) {
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
