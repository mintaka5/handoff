package org.white5moke.handoff.client;

import java.security.*;

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
}
