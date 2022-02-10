package org.white5moke.handoff.document;

import io.leonard.Base58;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.white5moke.handoff.client.SignThis;
import org.white5moke.handoff.know.PoW;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class KeyDocument {
    public static final String ENC_OBJ_NAME = "enc";
    public static final String SIGN_OBJ_NAME = "sign";
    public static final String DOC_TIME_NAME = "time" ;
    public static final String DOC_MSG_NAME = "msg";
    public static final String DOC_HASH_NAME = "hash";
    public static final String DOC_SIGN_NAME = "signature";
    public static final String DOC_WORK_NAME = "work";
    private EncryptionDocument encryptionDocument;
    private SigningDocument signingDocument;
    private PoW pow;
    private long timestamp;
    private byte[] message;
    private byte[] hash;

    public KeyDocument() {
        message = "".getBytes(StandardCharsets.UTF_8);
        timestamp = Instant.now().toEpochMilli();

        encryptionDocument = new EncryptionDocument();
        signingDocument = new SigningDocument();

        try {
            pow = new PoW(aggregateBytes(), 1);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public KeyDocument(String message) {
        this.message = message.trim().getBytes(StandardCharsets.UTF_8);
        timestamp = Instant.now().toEpochMilli();

        encryptionDocument = new EncryptionDocument();
        signingDocument = new SigningDocument();

        try {
            pow = new PoW(aggregateBytes(), 1);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public KeyDocument(JSONObject j) {
        setEncryptionDocument(new EncryptionDocument(j.getJSONObject("enc")));
    }

    public byte[] aggregateBytes() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        JSONObject j = toJson();

        return j.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static String ez(byte[] encoded) throws IOException {
        return Base58.encode(compress(encoded));
    }

    public static byte[] notEz(String decoded) throws DataFormatException {
        return decompress(Base58.decode(decoded));
    }

    private static byte[] decompress(byte[] decode) throws DataFormatException {
        Inflater decomp = new Inflater();
        byte[] buffer = new byte[1024];
        decomp.setInput(decode);
        decomp.inflate(buffer);
        decomp.end();

        return buffer;
    }

    private static byte[] compress(byte[] encoded) throws IOException {
        Deflater comp = new Deflater();
        comp.setLevel(Deflater.BEST_COMPRESSION);
        comp.setInput(encoded);
        comp.finish();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(encoded.length);
        byte[] buffer = new byte[1024];

        while(!comp.finished()) {
            int bytesCompressed = comp.deflate(buffer);
            bos.write(buffer, 0, bytesCompressed);
        }

        bos.close();

        byte[] compressedBs = bos.toByteArray();

        return compressedBs;
    }

    public EncryptionDocument getEncryptionDocument() {
        return encryptionDocument;
    }

    public SigningDocument getSigningDocument() {
        return signingDocument;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getMessage() {
        return message;
    }

    public byte[] getHash() {
        return hash;
    }

    @Override
    public String toString() {
        try {
            JSONObject j = toJson();
            return j.toString();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return "";
    }

    public JSONObject toJson() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        JSONObject j = new JSONObject();

        JSONObject encJson = encryptionDocument.toJson();
        JSONObject signJson = signingDocument.toJson();
        JSONObject powJson = pow.toJson();

        j.put(ENC_OBJ_NAME, encJson);
        j.put(SIGN_OBJ_NAME, signJson);
        j.put(DOC_TIME_NAME, timestamp);
        j.put(DOC_MSG_NAME, Base64.getEncoder().encodeToString(getMessage()));

        String jash = DigestUtils.sha256Hex(j.toString());
        j.put(DOC_HASH_NAME, jash);

        j.put(DOC_SIGN_NAME, SignThis.sign(
                j.toString().getBytes(StandardCharsets.UTF_8),
                encryptionDocument.getKeyPair().getPrivate())
        );

        // TODO : may move this before signing and hash...
        j.put(DOC_WORK_NAME, powJson);

        return j;
    }

    public void setEncryptionDocument(EncryptionDocument encryptionDocument) {
        this.encryptionDocument = encryptionDocument;
    }

    public void setSigningDocument(SigningDocument signingDocument) {
        this.signingDocument = signingDocument;
    }

    public void setPow(PoW pow) {
        this.pow = pow;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }
}
