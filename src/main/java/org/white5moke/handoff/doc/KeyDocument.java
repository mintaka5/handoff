package org.white5moke.handoff.doc;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.white5moke.handoff.Utilities;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class KeyDocument {
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
        j.put("encrypting", getEncrypting().toJson());
        j.put("message", getMessage());
        j.put("signing", getSigning().toJson());
        j.put("tag", getTag());
        j.put("timestamp", getTimestamp());

        /**
         * place all JSON needing hashing before this line
         */
        j.put("hash", getHash());

        return j;
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
