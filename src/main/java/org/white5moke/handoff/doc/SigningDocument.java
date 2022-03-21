package org.white5moke.handoff.doc;

import org.json.JSONObject;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SigningDocument {
    private static final String PRIV_JSON_KEY = "priv";
    private static final String PUB_JSON_KEY = "pub";

    private KeyPair keyPair;
    private KeyStore keyStore;

    public SigningDocument() {}

    public static SigningDocument fromJson(JSONObject json) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        SigningDocument doc = new SigningDocument();

        byte[] privBs = Base64.getDecoder().decode(json.getString(PRIV_JSON_KEY).strip());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privBs);
        KeyFactory factory = KeyFactory.getInstance("EC");
        PrivateKey privKey = factory.generatePrivate(spec);

        byte[] pubBs = Base64.getDecoder().decode(json.getString(PUB_JSON_KEY).strip());
        X509EncodedKeySpec spec1 = new X509EncodedKeySpec(pubBs);
        PublicKey pubKey = factory.generatePublic(spec1);
        KeyPair pair = new KeyPair(pubKey, privKey);

        doc.setKeyPair(pair);

        return doc;
    }

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

    public void setKeyStore(KeyStore k) {
        this.keyStore = k;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }
}
