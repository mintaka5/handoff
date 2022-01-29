package org.white5moke.handoff;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.white5moke.handoff.client.HandoffClient;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

public class App {
    public App() throws Exception {
        //SignThis signThis = new SignThis(new byte[16], privateKey);
        new HandoffClient();
    }

    /**
     * This is just a scratch method. DO NOT DELETE!
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static void a1() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        final String SECP = "secp256r1";

        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");

        ECGenParameterSpec curve = new ECGenParameterSpec(SECP);
        gen.initialize(curve);

        KeyPair pair = gen.generateKeyPair();

        ECPrivateKey priv = (ECPrivateKey) pair.getPrivate();
        ECPublicKey pub = (ECPublicKey) pair.getPublic();

        byte[] privBytes = priv.getEncoded();
        byte[] pubBytes = pub.getEncoded();
        String privB64 = Base64.getEncoder().encodeToString(privBytes);
        String pubB64 = Base64.getEncoder().encodeToString(pubBytes);
        long timestamp = Instant.now().toEpochMilli();

        JSONObject jo = new JSONObject();
        jo.put("priv", privB64);
        jo.put("publ", pubB64);
        jo.put("time", timestamp);
        String hash = DigestUtils.sha256Hex(
                String.format(
                        "%s%s%s",
                        privB64, pubB64, String.valueOf(timestamp)
                )
        );
        jo.put("hash", hash);

        PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(privBytes);
        // convert ECPrivateKey to normal PrivateKey
        KeyFactory factory = KeyFactory.getInstance("EC");
        PrivateKey pk = factory.generatePrivate(pkcs8);
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(pk);
        // send json into signing
        signature.update(jo.toString().getBytes(StandardCharsets.UTF_8));
        byte[] sing = signature.sign();
        jo.put("sign", Base64.getEncoder().encodeToString(sing));

        Path storePath = Path.of(System.getProperty("user.home").toString(), ".handoff", hash);

        Files.createDirectories(storePath.getParent());
        Files.createFile(storePath);

        Files.write(storePath, jo.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

        // get all files in assigned directory (~/.handoff)
        Path currentPath = Files.list(
                        storePath.getParent())
                .filter(f -> !Files.isDirectory(f))
                .sorted((p1, p2) -> Long.valueOf(p2.toFile().lastModified()).compareTo(p1.toFile().lastModified())
                ).findFirst().get();

        // use the last modified file as the current identity key
        byte[] contentBytes = Files.readAllBytes(currentPath);

        JSONObject keyJson = new JSONObject(new String(contentBytes, StandardCharsets.UTF_8));

        // start to verify signature
        X509EncodedKeySpec x509 = new X509EncodedKeySpec(Base64.getDecoder().decode(keyJson.getString("publ")));
        KeyFactory skf = KeyFactory.getInstance("EC");
        PublicKey pubk = skf.generatePublic(x509);

        Signature vsign = Signature.getInstance("SHA256withECDSA");
        vsign.initVerify(pubk);

        // get signature, remove from json, and verify that it's valid
        byte[] fsignBytes = Base64.getDecoder().decode(keyJson.getString("sign").getBytes(StandardCharsets.UTF_8));
        keyJson.remove("sign");
        String msg = keyJson.toString();
        vsign.update(msg.getBytes(StandardCharsets.UTF_8));

        boolean isSigValid = vsign.verify(fsignBytes);

        if (isSigValid == true) { // a valid key
            // go forth and encrypt

            String pubStr = keyJson.getString("publ");
            byte[] decodedPubKey = Base64.getDecoder().decode(pubStr);
            X509EncodedKeySpec x509PubKeySpec = new X509EncodedKeySpec(decodedPubKey);
            KeyFactory kf = KeyFactory.getInstance("EC");
            PublicKey pubKeyFromFile = kf.generatePublic(x509PubKeySpec);
            SecretKey secretKey = new SecretKeySpec(pubKeyFromFile.getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);


            // get entire file contents again to encrypt all that we removed

            //byte[] encMsg = cipher.doFinal(contentBytes);

            //System.out.println(String.format("encrypted message: %s", Base64.getEncoder().encodeToString(encMsg)));
        }
    }

    public static void main(String... args) throws Exception {
        new App();
    }
}