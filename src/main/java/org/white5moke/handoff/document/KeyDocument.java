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
import java.util.zip.Deflater;

public class KeyDocument {
    private static final String ENC_OBJ_NAME = "enc";
    private static final String SIGN_OBJ_NAME = "sign";
    private static final String DOC_TIME_NAME = "time" ;
    private static final String DOC_MSG_NAME = "msg";
    private static final String DOC_HASH_NAME = "hash";
    private static final String DOC_SIGN_NAME = "signature";
    private static final String DOC_WORK_NAME = "work";
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
            pow = new PoW(aggregateForPoW(), 1);
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
            pow = new PoW(aggregateForPoW(), 1);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private byte[] aggregateForPoW() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
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

        return j.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static String ez(byte[] encoded) throws IOException {
        return Base58.encode(compress(encoded));
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
}
