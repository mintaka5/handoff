package org.white5moke.handoff.document;

import java.security.*;

public class SignIt {
    public static String ALGORITHM = "SHA256withECDSA";

    public SignIt() {}

    public static byte[] sign(byte[] data, PrivateKey privateKey) throws NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance(ALGORITHM);
        sig.initSign(privateKey);
        sig.update(data);

        return sig.sign();
    }

    public static boolean isValid(byte[] original, byte[] signedData, PublicKey publicKey) throws
            NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        boolean is = false;

        Signature verity = Signature.getInstance(ALGORITHM);
        verity.initVerify(publicKey);
        verity.update(original);

        return verity.verify(signedData);
    }
}
