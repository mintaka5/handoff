package org.white5moke.handoff.doc;

import org.apache.commons.codec.binary.StringUtils;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class KeyPolicy {
    private static final int TAG_LEN = 8;
    private static final int SIG_LEN = 72;
    private static final int HASH_LEN = 64;

    private KeyDocument keyDocument;

    public KeyPolicy(KeyDocument keyDocument) {
        setKetDocument(keyDocument);
    }

    public void setKetDocument(KeyDocument keyDocument) {
        this.keyDocument = keyDocument;
    }

    public KeyDocument getKeyDocument() {
        return keyDocument;
    }

    public void setKeyDocument(KeyDocument keyDocument) {
        this.keyDocument = keyDocument;
    }

    public byte[] toBytes() {
        KeyDocument doc = getKeyDocument();

        byte[] tagBs = new byte[TAG_LEN];
        byte[] sigBs = new byte[SIG_LEN];
        byte[] hashBs = new byte[HASH_LEN];

        ByteBuffer buffer = ByteBuffer.allocate(
                Long.BYTES
                + tagBs.length
                + sigBs.length
                + hashBs.length
        );

        tagBs = StringUtils.getBytesUtf8(doc.getTag());
        sigBs = doc.getSignature();
        hashBs = StringUtils.getBytesUtf8(doc.getHash());

        buffer.putLong(doc.getTimestamp())
                .put(tagBs)
                .put(sigBs)
                .put(hashBs);

        return buffer.array();
    }

    @Override
    public String toString() {
        return "";
    }

    public static void main(String[] args) {
        Arrays.asList(
                "7d277f79a7322352d774f51157a8a08e75811bba97d6c00122a2fc9b8d31bc9f",
                "faaaee724b4577779be1784ebbe676a70fa33c5cb2893840f50796f7bb667ef3",
                "18e077bed0c92567df23db34be8fa72fcb22a08e578d23ddd403047f648665e8"
        ).forEach(h -> {
            Path keyDocFilename = Path.of(System.getProperty("user.home"), ".handoff", h);
            try {
                KeyDocument doc = TheStore.docToKeyDocument(keyDocFilename);
                KeyPolicy policy = new KeyPolicy(doc);
                String policyS = new String(policy.toBytes());
                System.out.println(policyS);
                System.out.println(policy.toBytes().length);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        });
    }
}
