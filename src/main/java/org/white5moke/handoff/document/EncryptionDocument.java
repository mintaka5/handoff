package org.white5moke.handoff.document;

import org.json.JSONObject;
import org.white5moke.handoff.client.Ez;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncryptionDocument {
    public static final String ALGORITHM = "RSA";
    public static final int KEY_SIZE = 2048;
    private static final String JSON_PRIV_KEY = "priv";
    private static final String JSON_PUB_KEY = "pub";

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private Ez privEz = Ez.getInstance();
    private Ez pubEz = Ez.getInstance();

    public KeyPair generate() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM);
        gen.initialize(KEY_SIZE);
        KeyPair pair = gen.generateKeyPair();

        setPrivateKey(pair.getPrivate());
        setPublicKey(pair.getPublic());

        return pair;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String toString() {
        JSONObject j = new JSONObject();

        j.put(JSON_PRIV_KEY, privEz.ez(getPrivateKey().getEncoded()));
        j.put(JSON_PUB_KEY, pubEz.ez(getPublicKey().getEncoded()));

        return j.toString();
    }

    public static EncryptionDocument fromJson(JSONObject json) throws NoSuchAlgorithmException, InvalidKeySpecException {
        EncryptionDocument encDoc = new EncryptionDocument();

        byte[] pubBs = Ez.getInstance().notEz(json.getString(JSON_PUB_KEY).strip());
        byte[] privBs = Ez.getInstance().notEz(json.getString(JSON_PRIV_KEY).strip());

        PKCS8EncodedKeySpec spec1 = new PKCS8EncodedKeySpec(privBs);
        KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privKey = factory.generatePrivate(spec1);

        X509EncodedKeySpec spec2 = new X509EncodedKeySpec(pubBs);
        PublicKey pubKey = factory.generatePublic(spec2);

        encDoc.setPrivateKey(privKey);
        encDoc.setPublicKey(pubKey);

        return encDoc;
    }
}