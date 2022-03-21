package org.white5moke.handoff.doc;

import org.json.JSONObject;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncryptionDocument {
    public static final String PUB_JSON_KEY = "pub";
    public static final String PRIV_JSON_KEY = "priv";

    private KeyPair keyPair;

    public EncryptionDocument() {}

    public static EncryptionDocument fromJson(JSONObject json) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        EncryptionDocument doc = new EncryptionDocument();

        byte[] privBs = Base64.getDecoder().decode(json.getString(PRIV_JSON_KEY).strip());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privBs);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey privKey = factory.generatePrivate(spec);

        byte[] pubBs = Base64.getDecoder().decode(json.getString(PUB_JSON_KEY).strip());
        X509EncodedKeySpec spec1 = new X509EncodedKeySpec(pubBs);
        PublicKey pubKey = factory.generatePublic(spec1);
        KeyPair pair = new KeyPair(pubKey, privKey);

        doc.setKeyPair(pair);

        return doc;
    }

    /**
     *
     * @param keySize
     */
    public void generate(int keySize) {
        KeyPairGenerator gen = null;
        try {
            gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(keySize);
            setKeyPair(gen.generateKeyPair());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJson() {
        JSONObject j = new JSONObject();

        j.put(PRIV_JSON_KEY, Base64.getEncoder().encodeToString(getKeyPair().getPrivate().getEncoded()));
        j.put(PUB_JSON_KEY, Base64.getEncoder().encodeToString(getKeyPair().getPublic().getEncoded()));

        return j;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }
}
