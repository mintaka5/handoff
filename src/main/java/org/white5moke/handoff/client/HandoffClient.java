package org.white5moke.handoff.client;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class HandoffClient {
    private Path home = Path.of(System.getProperty("user.home"), ".handoff");
    private Scanner scan = new Scanner(System.in);
    private List<Path> hashList;
    private String currentDocumentHash = StringUtils.EMPTY;

    public HandoffClient() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        runLoop();
    }

    private void runLoop() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        while (true) {
            System.out.print(Txt.C.GREEN.get() + "> ");

            String input = scan.nextLine().strip().toLowerCase();

            if (input.equals("bye")) {
                sayGoodbye();
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
                    list();
                }
                case "gen" -> {
                    generateKey(theMsg);
                }
                case "use" -> {
                    useKey(theMsg);
                }
                case "help" -> {
                    System.out.println(Txt.C.BRIGHT_MAGENTA.get() + "help " + Txt.C.RESET.get() + ": help information.");
                    System.out.println(Txt.C.BRIGHT_MAGENTA.get() + "echo " + Txt.C.RESET.get() + ": prints user input. " + Txt.C.BRIGHT_CYAN.get() + "`echo <any text string here>`");
                    System.out.println(Txt.C.BRIGHT_MAGENTA.get() + "list " + Txt.C.RESET.get() + ": list all key documents. " + Txt.C.BRIGHT_CYAN.get() + "`list`");
                    System.out.println(Txt.C.BRIGHT_MAGENTA.get() + "gen  " + Txt.C.RESET.get() + ": generate a new key document. " + Txt.C.BRIGHT_CYAN.get() + "`gen`");
                    System.out.print(Txt.C.BRIGHT_MAGENTA.get() + "use  " + Txt.C.RESET.get() + ": designate currently used key document." + Txt.C.BRIGHT_CYAN.get() + " `use <paste key document hash here>`");
                    System.out.println(Txt.C.RESET.get());
                }
                default -> {}
            }
        }
    }

    private void useKey(String msg) throws IOException {
        hashList = Files
                .list(home)
                .sorted((f1, f2) -> Long.valueOf(f2.toFile().lastModified()).compareTo(f1.toFile().lastModified()))
                .toList();

        System.out.print(Txt.C.GREEN.get() + "enter a number > " + Txt.C.RESET.get());
        int selection = scan.nextInt();

        if(selection > hashList.size()-1 || selection < 0) {
            System.out.println(Txt.C.BRIGHT_RED.get() + "not a valid selection." + Txt.C.RESET.get());
        } else {
            Path g = hashList.get(selection);
            currentDocumentHash = g.getFileName().toString();

            System.out.println(Txt.C.BRIGHT_BLUE.get() + String.format("current key document is set to `%s`", currentDocumentHash) + Txt.C.RESET.get());
        }
    }

    private void sayGoodbye() {
        System.out.println(Txt.C.BRIGHT_RED.get() + "exiting..." + Txt.C.RESET.get());
        scan.close();
        System.exit(0);
    }

    private void list() throws IOException {
        hashList = Files
                .list(home)
                .sorted((f1, f2) -> Long.valueOf(f2.toFile().lastModified()).compareTo(f1.toFile().lastModified()))
                .toList();

        AtomicInteger i = new AtomicInteger();
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

            System.out.println(Txt.C.BRIGHT_BLUE.get() + s + Txt.C.RESET.get());
        });

        System.out.println(Txt.C.RED.get() + "select a hash, by using the command `use` and when prompted provide" +
                " `n`, `n` being the number preceeding the hash i.e. `1)`" + Txt.C.RESET.get());

        return;
    }

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
}
