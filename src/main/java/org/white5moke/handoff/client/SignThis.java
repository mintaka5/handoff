package org.white5moke.handoff.client;

import io.leonard.Base58;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

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

    public static byte[] compress(byte[] signature) throws IOException {
        Deflater comp = new Deflater();
        comp.setLevel(Deflater.BEST_COMPRESSION);
        comp.deflate(signature);

        return signature;
    }
}
