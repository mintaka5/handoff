package org.white5moke.handoff.document;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.white5moke.handoff.client.Ez;
import org.white5moke.handoff.know.PoW;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.zip.DataFormatException;

public class KeyDocument {
    private static final String JSON_HASH_KEY = "hash";
    private static final String JSON_MSG_KEY = "msg";
    private static final String JSON_TIME_KEY = "time";
    private static final String JSON_ENC_KEY = "enc";
    private static final String JSON_SIGN_KEY = "sign";
    private static final String JSON_POW_KEY = "pow";
    private static final String JSON_SIG_KEY = "sig";
    private long timestamp;
    private String message;
    private String hash;
    private String signature;
    private SigningDocument signingDocument;
    private EncryptionDocument encryptionDocument;
    private PoW pow;

    public KeyDocument() {}

    public void generate(String message) {
        setMessage(message.strip());
        setTimestamp(Instant.now().toEpochMilli());
        setSigningDocument(new SigningDocument());
        setEncryptionDocument(new EncryptionDocument());

        try {
            getSigningDocument().generate();
            getEncryptionDocument().generate();

            PoW getAJob = new PoW(1);
            getAJob.work(getMessage().getBytes(StandardCharsets.UTF_8));
            setPow(getAJob);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        setHash(DigestUtils.sha256Hex(aggregateJsonBytes()));

        sign(aggregateJsonBytes());
    }

    /**
     * build out key doc based on json from file
     * @param json
     * @return
     */
    public void fromJson(JSONObject json) {
        setMessage(json.getString(JSON_MSG_KEY).strip());
        setTimestamp(json.getLong(JSON_TIME_KEY));

        try {
            SigningDocument signing = SigningDocument.fromJson(json.getJSONObject(JSON_SIGN_KEY));
            setSigningDocument(signing);

            EncryptionDocument encrypt = EncryptionDocument.fromJson(json.getJSONObject(JSON_ENC_KEY));
            setEncryptionDocument(encrypt);

            PoW pow = PoW.fromJson(json.getJSONObject(JSON_POW_KEY));
            setPow(pow);
        } catch (NoSuchAlgorithmException | DataFormatException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        setHash(json.getString(JSON_HASH_KEY));

        setSignature(json.getString(JSON_SIG_KEY));
    }

    public boolean isSignatureOk() {
        boolean is = false;
        byte[] sigBs = Ez.getInstance().notEz(getSignature());

        // need to remove signature because it's not included in original signing
        JSONObject json = new JSONObject(toString());
        json.remove(JSON_SIG_KEY);
        /* TODO : get json string into bytes
        */

        return is;
    }

    /**
     * aggregate entire JSON doc and sign it
     * @param data
     */
    private void sign(byte[] data) {
        try {
            byte[] signatureBs = SignIt.sign(data, getSigningDocument().getPrivateKey());
            String signatureS = Ez.getInstance().ez(signatureBs);
            setSignature(signatureS);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
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

        JSONObject jEnc = new JSONObject(getEncryptionDocument().toString());
        j.put(JSON_ENC_KEY, jEnc);

        JSONObject jSign = new JSONObject(getSigningDocument().toString());
        j.put(JSON_SIGN_KEY, jSign);

        JSONObject jPow = getPow().toJson();
        j.put(JSON_POW_KEY, jPow);

        j.put(JSON_HASH_KEY, getHash());

        j.put(JSON_SIG_KEY, getSignature());

        return j.toString();
    }

    public PoW getPow() {
        return pow;
    }

    public void setPow(PoW pow) {
        this.pow = pow;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
