package org.white5moke.handoff.doc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class KeyPolicy {
    private static final int TAG_LEN = 8;
    private static final int SIG_LEN = 72;
    private static final int HASH_LEN = 64;

    private KeyDocument keyDocument;

    public KeyPolicy(KeyDocument keyDocument) {
        setKeyDocument(keyDocument);
    }

    public KeyDocument getKeyDocument() {
        return keyDocument;
    }

    public void setKeyDocument(KeyDocument keyDocument) {
        this.keyDocument = keyDocument;
    }

    public byte[] toBytes() {
        KeyDocument doc = getKeyDocument();

        int addEmUp = HASH_LEN + SIG_LEN + TAG_LEN + Long.BYTES;
        return ByteBuffer.allocate(addEmUp)
                .put(doc.getTag().getBytes(StandardCharsets.UTF_8))
                .putLong(doc.getTimestamp())
                .put(doc.getHash().getBytes(StandardCharsets.UTF_8))
                .put(doc.getSignature())
                .array();
    }

    @Override
    public String toString() {
        return "";
    }

    public static void main(String[] args) {
        try {
            Files.list(Path.of(System.getProperty("user.home"), ".handoff")).forEach(filename -> {
                try {
                    KeyDocument doc = TheStore.docToKeyDocument(filename);
                    KeyPolicy policy = new KeyPolicy(doc);

                    byte[] encodedPolicy = policy.toBytes();

                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
