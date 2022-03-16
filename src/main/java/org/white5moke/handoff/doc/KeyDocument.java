package org.white5moke.handoff.doc;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.white5moke.handoff.Utilities;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Base64;

public class KeyDocument {
    private static final String HASH_JSON_KEY = "hash";
    private static final String TAG_JSON_KEY = "tag";
    private static final String MSG_JSON_KEY = "message";
    private static final String TIME_JSON_KEY = "timestamp";
    private static final String SIGN_JSON_KEY = "signing";
    private static final String ENC_JSON_KEY = "encrypting";

    private SigningDocument signing;
    private EncryptionDocument encrypting;
    private long timestamp;
    private String message = "";
    private String tag;
    private String hash;

    public KeyDocument() {}

    /**
     * generate whole document
     * @param signKeySize signing key size 256 is a good #
     */
    public void generate(String msg, int signKeySize, int encKeySize) {
        setTag(Utilities.randomID(4));

        // time is needed
        setTimestamp(Instant.now().toEpochMilli());

        // message
        setMessage(msg.strip());

        // generate signing document
        setSigning(new SigningDocument());
        getSigning().generate(signKeySize);

        // set up encryption document
        setEncrypting(new EncryptionDocument());
        getEncrypting().generate(encKeySize);

        // hash the whole json string for integrity
        setHash(DigestUtils.sha256Hex(toString()));
    }

    public SigningDocument getSigning() {
        return signing;
    }

    public void setSigning(SigningDocument signing) {
        this.signing = signing;
    }

    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        j.put(ENC_JSON_KEY, getEncrypting().toJson());
        j.put(MSG_JSON_KEY, getMessage());
        j.put(SIGN_JSON_KEY, getSigning().toJson());
        j.put(TAG_JSON_KEY, getTag());
        j.put(TIME_JSON_KEY, getTimestamp());

        /**
         * place all JSON needing hashing before this line
         */
        j.put(HASH_JSON_KEY, getHash());

        return j;
    }

    public static KeyDocument fromJson(JSONObject json) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyDocument doc = new KeyDocument();
        doc.setHash(json.getString(HASH_JSON_KEY).strip());
        doc.setTag(json.getString(TAG_JSON_KEY).strip());
        doc.setMessage(json.getString(MSG_JSON_KEY).strip());
        doc.setTimestamp(json.getLong(TIME_JSON_KEY));

        // convert keys to Java objects
        doc.setSigning(SigningDocument.fromJson(json.getJSONObject(SIGN_JSON_KEY)));
        doc.setEncrypting(EncryptionDocument.fromJson(json.getJSONObject(ENC_JSON_KEY)));

        return doc;
    }

    @Override
    public String toString() {
        return toJson().toString();
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

    public EncryptionDocument getEncrypting() {
        return encrypting;
    }

    public void setEncrypting(EncryptionDocument encrypting) {
        this.encrypting = encrypting;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
