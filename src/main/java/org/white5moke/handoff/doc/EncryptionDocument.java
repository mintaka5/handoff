package org.white5moke.handoff.doc;

import org.json.JSONObject;
import org.white5moke.handoff.Utilities;
import org.white5moke.handoff.Utilities.KeyWrapType;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncryptionDocument {
    private KeyPair keyPair;

    public EncryptionDocument() {}

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

        j.put("priv", Base64.getEncoder().encodeToString(getKeyPair().getPrivate().getEncoded()));
        j.put("pub", Base64.getEncoder().encodeToString(getKeyPair().getPublic().getEncoded()));

        return j;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }
}
