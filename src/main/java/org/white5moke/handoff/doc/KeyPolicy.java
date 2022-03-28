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

        ByteBuffer timeBs = ByteBuffer.allocate(Long.BYTES).putLong(doc.getTimestamp());
        ByteBuffer hashBs = ByteBuffer.allocate(TAG_LEN).put(doc.getTag().getBytes(StandardCharsets.UTF_8));
        ByteBuffer sigBs = ByteBuffer.allocate(SIG_LEN).put(doc.getSignature());

        int addEmUp = HASH_LEN + SIG_LEN + TAG_LEN + Long.BYTES;
        return ByteBuffer.allocate(addEmUp)
                .put(timeBs.array())
                .put(hashBs.array())
                .put(sigBs.array())
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
                    /*System.out.println("-----");
                    System.out.println(doc.getHash() + " [" + doc.getHash().getBytes().length + "]");
                    System.out.println(doc.getTimestamp() + " [" + Long.BYTES + "]");
                    System.out.println(doc.getMessage() + " [" + doc.getMessage().getBytes().length + "]");
                    System.out.println(doc.getTag() + " [" + doc.getTag().getBytes().length + "]");
                    System.out.println(Base64.getEncoder().encodeToString(doc.getSignature()) + " [" + doc.getSignature().length + "]");
                    System.out.println("-----");*/
                    byte[] encodedPolicy = policy.toBytes();
                    System.out.println(
                            new String(encodedPolicy, StandardCharsets.UTF_8)
                                    + " byte len. [" + policy.toBytes().length
                                    + "]"
                    );
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
