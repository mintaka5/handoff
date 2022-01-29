package org.white5moke.handoff.client;

import io.leonard.Base58;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class SignThis {
    public static byte[] sign(byte[] msg, PrivateKey privKey) throws NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        Signature signing = Signature.getInstance("SHA256withECDSA");
        signing.initSign(privKey);
        signing.update(msg);

        return signing.sign();
    }

    public static boolean isValidSignature(byte[] origMsg, PublicKey pubKey, byte[] signedMsg) throws
            NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature verity = Signature.getInstance("SHA256withECDSA");
        verity.initVerify(pubKey);
        verity.update(origMsg);

        return verity.verify(signedMsg);
    }

    public static byte[] compress(byte[] stuff) throws IOException {
        Deflater comp = new Deflater();
        comp.setLevel(Deflater.BEST_COMPRESSION);
        comp.deflate(stuff);

        return stuff;
    }

    public static byte[] decompress(byte[] stuff) throws DataFormatException {
        Inflater decomp = new Inflater();
        byte[] ns = stuff;
        decomp.inflate(ns);

        return ns;
    }

    public static String ez(byte[] stuff) throws IOException {
        return Base58.encode(compress(stuff));
    }

    public static byte[] notEz(String stuff) throws DataFormatException {
        return decompress(Base58.decode(stuff));
    }
}
