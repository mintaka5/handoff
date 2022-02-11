package org.white5moke.handoff.document;

import org.json.JSONObject;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.zip.DataFormatException;

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

    public SigningDocument(JSONObject j) {
        try {
            byte[] privBs = KeyDocument.notEz(j.getString(JSON_PRIV_KEY));
            byte[] pubBs = KeyDocument.notEz(j.getString(JSON_PUB_KEY));

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

    public KeyPair getKeyPair() {
        return this.keyPair;
    }
}
