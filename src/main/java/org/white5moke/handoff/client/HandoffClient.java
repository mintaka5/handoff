package org.white5moke.handoff.client;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.white5moke.handoff.know.PoW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class HandoffClient {
    private final Path home;
    private final Scanner scan = new Scanner(System.in);
    private List<Path> hashList;
    private String currentDocumentHash = StringUtils.EMPTY;

    public HandoffClient() throws IOException, NoSuchAlgorithmException, SignatureException,
            InvalidKeyException, InvalidKeySpecException {
        // establish file structure
        home = Path.of(System.getProperty("user.home"), ".handoff");
        Files.createDirectories(home.getParent());

        runLoop();
    }

    private void runLoop() throws IOException, NoSuchAlgorithmException, SignatureException,
            InvalidKeyException, InvalidKeySpecException {
        while (true) {
            System.out.print("> ");

            String input = scan.nextLine().strip().toLowerCase();

            if (input.equals("bye")) {
                sayGoodbye();
                return;
            }

            String[] strings = StringUtils.split(input, StringUtils.SPACE);

            if (strings.length <= 0) continue;

            String[] theRest = Arrays.copyOfRange(strings, 1, strings.length);
            String theMsg = StringUtils.join(theRest, StringUtils.SPACE);

            switch (strings[0]) {
                case "cur" -> current();
                case "del" -> delete();
                case "echo" -> echo(theMsg);
                case "gen" -> generateKey(theMsg);
                case "help" -> help();
                case "list" -> list();
                case "peek" -> peek(theMsg);
                case "sign" -> signMessage(theMsg);
                case "vsign" -> verifyMessageSignature(theMsg);
                case "use" -> useKey(theMsg);
                default -> {}
            }
        }
    }

    private void verifyMessageSignature(String msg) {
        System.out.print("key? >");
        String keyS = scan.nextLine();
        System.out.print("orig. message: >");
        String origMsgS = scan.nextLine();
        System.out.print("signature: >");
        String signatureS = scan.nextLine();

        System.out.printf("key: `%s`\nmessage: `%s`; signature: `%s`%n", keyS, origMsgS, signatureS);
    }

    private void signMessage(String msg) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidKeyException, SignatureException {
        byte[] msgBs = msg.trim().getBytes(StandardCharsets.UTF_8);

        byte[] signature = SignThis.sign(msgBs, getPrivateKeyFromFile());

        System.out.printf("signature: `%s`\nfor the message `%s`%n", Base64.getEncoder().encodeToString(signature),
                msg.trim());
    }

    private PublicKey getPublicKeyFromFile() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if(currentDocumentHash.isEmpty()) throw new IllegalArgumentException("no key document is set");

        System.out.printf("your active key document is `%s`%n", currentDocumentHash);
        Path p = Path.of(home.toString(), currentDocumentHash);
        String doc = Files.readString(p);
        JSONObject j = new JSONObject(doc);

        byte[] pubBs = Base64.getDecoder().decode(j.getJSONObject("signing").getString("pub").trim());

        X509EncodedKeySpec spec = new X509EncodedKeySpec(pubBs);
        KeyFactory factory = KeyFactory.getInstance("EC");

        return factory.generatePublic(spec);
    }

    private PrivateKey getPrivateKeyFromFile() throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException {
        if(currentDocumentHash.isEmpty()) throw new IllegalArgumentException("no key document is set");

        System.out.printf("your active key document is `%s`%n", currentDocumentHash);
        Path p = Path.of(home.toString(), currentDocumentHash);
        String doc = Files.readString(p);
        JSONObject j = new JSONObject(doc);
        byte[] privBs = Base64.getDecoder().decode(j.getJSONObject("signing").getString("priv").trim());

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privBs);
        KeyFactory factory = KeyFactory.getInstance("EC");

        return factory.generatePrivate(spec);
    }

    private void help() {
        System.out.println("bye : exit the app.");

        System.out.println("cur : what is your current key document `cur`");

        System.out.println("del : deletes all your key documents. be careful! `del`");

        System.out.println("echo : prints user input. `echo <any text string here>`");

        System.out.println("gen : generate a new key document. message is optional. `gen [<message>]`");

        System.out.println("help : help information.");

        System.out.println("list : list all key documents. sorted by most recent first `list`");

        System.out.println("peek : view the details of a key document `peek <# from `list`>`");

        System.out.println("use : designate currently used key document. `use <# from `list`>`");
    }

    private void delete() throws IOException {
        Files.list(home).forEach(f -> {
            try {
                Files.delete(f);
                System.out.printf("removed `%s`%n", f.getFileName().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("complete.");

        currentDocumentHash = StringUtils.EMPTY;
    }

    private void current() throws IOException {
        if (currentDocumentHash.isEmpty()) {
            System.out.println("no key document is active. try `use #` command.");
        } else {
            System.out.printf("your active key document is `%s`%n", currentDocumentHash);

            Path p = Path.of(home.toString(), currentDocumentHash);
            String keyDoc = Files.readString(p);
            JSONObject doc = new JSONObject(keyDoc);
        }
    }

    private void peek(String msg) throws IOException {
        String[] arr = StringUtils.split(msg, StringUtils.SPACE);

        // no empties, plz
        if (msg.isEmpty()) return;

        if (arr.length <= 0) return;

        if (!StringUtils.isNumeric(arr[0].strip())) {
            System.out.println("not a number! =/");

            return;
        }

        int selection = Integer.parseInt(arr[0].strip());

        hashList = Files
                .list(home)
                .sorted((f1, f2) -> Long.compare(f2.toFile().lastModified(), f1.toFile().lastModified()))
                .toList();

        if (selection > hashList.size() - 1 || selection < 0) {
            System.out.println("not a valid selection =(");

            return;
        }

        Path g = hashList.get(selection);
        currentDocumentHash = g.getFileName().toString();

        System.out.printf("peeking at key document `%s`%n", currentDocumentHash);

        String content = Files.readString(Path.of(home.toString(), currentDocumentHash));
        JSONObject doc = new JSONObject(content);

        System.out.printf("message: `%s`%n", doc.getString("msg"));
        System.out.printf("timestamp: %s%n", Instant.ofEpochMilli(doc.getLong("time"))
                .atZone(ZoneId.of("UTC")).toLocalDateTime().toString());
        System.out.printf("work factor: %d%n", doc.getJSONObject("pow").getLong("work"));
        System.out.printf("PoW hash: `%s`%n", doc.getJSONObject("pow").getString("hash"));
    }

    private void useKey(String msg) throws IOException, ArrayIndexOutOfBoundsException {
        String[] arr = StringUtils.split(msg, StringUtils.SPACE);

        // no empties, plz
        if (msg.isEmpty()) {
            if (!currentDocumentHash.isEmpty()) {
                System.out.printf("current key document is set to `%s`%n", currentDocumentHash);
            } else {
                System.out.println("no current key doc selected. select a key document (`use n` command). use `list` " +
                        "to show selections.");
            }

            return;
        }

        // if no first string is provided, user is querying current key document usage
        if (arr.length <= 0) return;

        // no alphas
        if (!StringUtils.isNumeric(arr[0].strip())) {
            System.out.println("not a number =/");
            return;
        }

        int selection = Integer.parseInt(arr[0].strip());

        hashList = Files.list(home)
                .sorted((f1, f2) -> Long.compare(f2.toFile().lastModified(), f1.toFile().lastModified()))
                .toList();

        if (selection > hashList.size() - 1 || selection < 0) {
            System.out.println("not a valid selection =(");
        } else {
            Path g = hashList.get(selection);
            currentDocumentHash = g.getFileName().toString();

            System.out.printf("current key document is set to `%s`%n", currentDocumentHash);
        }
    }

    private void sayGoodbye() {
        System.out.println("exiting...");
        scan.close();
        System.exit(0);
    }

    private void list() throws IOException {
        try {
            hashList = Files
                    .list(home)
                    .sorted((f1, f2) -> Long.compare(f2.toFile().lastModified(), f1.toFile().lastModified()))
                    .toList();
        } catch (NoSuchFileException | NullPointerException e) {
            System.out.println("no key documents available. use `gen` command.");
            return;
        }

        AtomicInteger i = new AtomicInteger(0);
        hashList.forEach(h -> {
            String s = String.format(
                    "%d) %s @ %s",
                    i.get(),
                    h.getFileName().toString(),
                    Instant.ofEpochMilli(h.toFile().lastModified())
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDateTime().toString()
            );
            i.getAndIncrement();

            System.out.println(s);
        });

        if (!currentDocumentHash.isEmpty()) {
            System.out.printf("current key document: `%s`%n", currentDocumentHash);
        }
    }

    /**
     * @param msg Generic string
     */
    private void generateKey(String msg) throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException, IOException {

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

        // TODO : PoW for value creation
        PoW pow = new PoW(keysJson.toString().getBytes(StandardCharsets.UTF_8), 1);
        System.out.println("doing the work...");

        JSONObject powJ = new JSONObject();
        powJ.put("hash", pow.getHash());
        powJ.put("work", pow.getNonce());
        keysJson.put("pow", powJ);

        System.out.printf("work completed by a factor of %d, requiring %d bit(s). signature: `%s`%n",
                pow.getNonce(), pow.getBitsNeeded(), pow.getHash());

        Path storePath = Path.of(System.getProperty("user.home"), ".handoff", hashStr);

        Files.createDirectories(storePath.getParent());
        Files.createFile(storePath);

        Files.writeString(storePath, keysJson.toString(), StandardOpenOption.CREATE);

        System.out.printf("key saved to document `%s`%n", storePath);
    }

    private void echo(String msg) {
        if (!msg.isEmpty()) {
            System.out.printf("ECHO: %s%n", msg.toUpperCase());
        } else {
            System.out.println("SILENCE");
        }
    }
}