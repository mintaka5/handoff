package org.white5moke.handeroffer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

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
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class App {
    public App() throws Exception {
        Path home = Path.of(System.getProperty("user.home"), ".handoff");

        while (true) {
            Scanner scan = new Scanner(System.in);

            System.out.print("> ");

            String input = scan.nextLine().strip().toLowerCase();

            if (input.equals("bye")) {
                scan.close();
                System.exit(0);
                return;
            }

            String[] strings = StringUtils.split(input, StringUtils.SPACE);

            if(strings.length <= 0) continue;

            String[] theRest = Arrays.copyOfRange(strings, 1, strings.length);
            String theMsg = StringUtils.join(theRest, StringUtils.SPACE);

           switch (strings[0]) {
               case "echo" -> {
                   echo(theMsg);
               }
               case "list" -> {
                   List<Path> hashList = Files.list(home).sorted((f1, f2) -> Long.valueOf(f2.toFile().lastModified()).compareTo(f1.toFile().lastModified())).toList();
                   hashList.forEach(h -> {
                       String s = String.format(
                               "%s @ %s",
                               h.getFileName().toString(),
                               Instant.ofEpochMilli(h.toFile().lastModified())
                                       .atZone(ZoneId.of("UTC"))
                                       .toLocalDateTime().toString()
                               );

                       System.out.println(s);
                   });
               }
               case "use" -> {

               }
               case "gen" -> {
                   generateKey(theMsg);
               }
               default -> {
               }
           }
        }
    }

    private void generateKey(String msg) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();

        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        String b64PrivKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String b64PubKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        String salt = msg.strip().toLowerCase();
        long timestamp = Instant.now().toEpochMilli();

        JSONObject encKeysJson = new JSONObject();
        encKeysJson.put("priv", b64PrivKey);
        encKeysJson.put("pub", b64PubKey);

        KeyPairGenerator sGen = KeyPairGenerator.getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        sGen.initialize(256, random);

        KeyPair sPair = sGen.generateKeyPair();
        PrivateKey sPrivKey = sPair.getPrivate();
        PublicKey sPubKey = sPair.getPublic();

        JSONObject signKeysJson = new JSONObject();
        signKeysJson.put("priv", Base64.getEncoder().encodeToString(sPrivKey.getEncoded()));
        signKeysJson.put("pub", Base64.getEncoder().encodeToString(sPubKey.getEncoded()));

        JSONObject keysJson = new JSONObject();
        keysJson.put("time", timestamp);
        keysJson.put("msg", salt);
        keysJson.put("enc", encKeysJson);

        keysJson.put("signing", signKeysJson);

        Signature signing = Signature.getInstance("SHA256withECDSA");
        signing.initSign(sPrivKey);

        String wholeJson = keysJson.toString(4);
        signing.update(wholeJson.getBytes(StandardCharsets.UTF_8));
        byte[] signature = signing.sign();
        keysJson.put("signature", Base64.getEncoder().encodeToString(signature));

        wholeJson = keysJson.toString();
        String hashStr = DigestUtils.sha256Hex(wholeJson.getBytes(StandardCharsets.UTF_8));
        keysJson.put("hash", hashStr);

        //System.out.println(keysJson.toString(4));

        Path storePath = Path.of(System.getProperty("user.home").toString(), ".handoff", hashStr);

        Files.createDirectories(storePath.getParent());
        Files.createFile(storePath);

        Files.write(storePath, keysJson.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

        System.out.println(String.format("key saved to `%s`", storePath.toString()));
    }

    private void echo(String msg) {
        if(!msg.isEmpty()) {
            System.out.println(String.format("ECHO: %s", msg.toUpperCase()));
        } else {
            System.out.println("SILENCE");
        }
    }

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
