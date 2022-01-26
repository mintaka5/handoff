package org.white5moke.handoff.client;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

public class Handoff {

    private PrivateKey privateKey;

    public Handoff() throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        //signingKeys();
        loop();
    }

    private void loop() throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        int i = 0;
        while(i < 5) {
            gen();
            i++;
        }
    }

    private void gen() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        //PrivateKey priv = privateKeyFromString();
        Signature sig = Signature.getInstance("SHA256withECDSA");
        //sig.initSign(priv, random);


        String timestampStr = StringUtils.rightPad(String.valueOf(Instant.now().toEpochMilli()), 16, StringUtils.SPACE);
        String randomStr = StringUtils.rightPad(Base64.getEncoder().encodeToString(random.generateSeed(16)), 32, StringUtils.SPACE);

        String msg = String.format("%s%s",
                timestampStr,
                randomStr);

        sig.update(msg.getBytes(StandardCharsets.UTF_8));
        byte[] signed = sig.sign();

        msg += StringUtils.rightPad(Base64.getEncoder().encodeToString(signed), 97, StringUtils.SPACE);



        System.out.println(msg);
    }

    private PrivateKey privateKeyFromString(String privateKey) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        byte[] pk = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pk);

        KeyFactory factory = KeyFactory.getInstance("EC");

        return factory.generatePrivate(spec);
    }

    private PublicKey publicKeyFromString(String publicKey) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        byte[] pk = Base64.getDecoder().decode(publicKey);

        EncodedKeySpec spec = new X509EncodedKeySpec(pk);

        KeyFactory factory = KeyFactory.getInstance("EC");
        PublicKey pubKey = factory.generatePublic(spec);

        return pubKey;
    }

    private KeyPair signingKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator sGen = KeyPairGenerator.getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        sGen.initialize(256, random);

        KeyPair sPair = sGen.generateKeyPair();

        System.out.println("pub: " + Base64.getEncoder().encodeToString(sPair.getPublic().getEncoded()));
        System.out.println("priv: " + Base64.getEncoder().encodeToString(sPair.getPrivate().getEncoded()));
        return sPair;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}
