package org.white5moke.handoff.document;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;



class EncryptionDocument {
    public static final String ALGORITHM = "RSA";
    public static final int KEY_SIZE = 2048;

    private PublicKey publicKey;
    private PrivateKey privateKey;

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
}

public class KeyDocument {
    private static final String JSON_HASH_KEY = "hash";
    private static final String JSON_MSG_KEY = "msg";
    private static final String JSON_TIME_KEY = "time";
    private static final String JSON_ENC_KEY = "enc";
    private static final String JSON_SIGN_KEY = "sign";
    private long timestamp;
    private String message;
    private String hash;
    private SigningDocument signingDocument;
    private EncryptionDocument encryptionDocument;

    public KeyDocument() {

    }

    public void generate(String message) {
        setMessage(message.strip());
        setTimestamp(Instant.now().toEpochMilli());
        setSigningDocument(new SigningDocument());
        setEncryptionDocument(new EncryptionDocument());
        try {
            getSigningDocument().generate();
            getEncryptionDocument().generate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        setHash(DigestUtils.sha256Hex(aggregateJsonBytes()));
    }

    private byte[] aggregateJsonBytes() {
        return toString().getBytes(StandardCharsets.UTF_8);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SigningDocument getSigningDocument() {
        return signingDocument;
    }

    public void setSigningDocument(SigningDocument signingDocument) {
        this.signingDocument = signingDocument;
    }

    public EncryptionDocument getEncryptionDocument() {
        return encryptionDocument;
    }

    public void setEncryptionDocument(EncryptionDocument encryptionDocument) {
        this.encryptionDocument = encryptionDocument;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        JSONObject j = new JSONObject();
        j.put(JSON_TIME_KEY, getTimestamp());
        j.put(JSON_MSG_KEY, getMessage());

        JSONObject jEnc = new JSONObject();
        j.put(JSON_ENC_KEY, jEnc);

        JSONObject jSign = new JSONObject(getSigningDocument().toString());
        j.put(JSON_SIGN_KEY, jSign);

        j.put(JSON_HASH_KEY, getHash());

        return j.toString(4);
    }
}
