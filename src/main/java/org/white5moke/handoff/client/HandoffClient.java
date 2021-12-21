package org.white5moke.handoff.client;

import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWord;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.white5moke.handoff.know.PoW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.diogonunes.jcolor.Ansi.colorize;

public class HandoffClient {
    private Path home = Path.of(System.getProperty("user.home"), ".handoff");
    private Scanner scan = new Scanner(System.in);
    private List<Path> hashList;
    private String currentDocumentHash = StringUtils.EMPTY;

    public HandoffClient() throws IOException, NoSuchAlgorithmException, SignatureException,
            InvalidKeyException {
        runLoop();
    }

    private void runLoop() throws IOException, NoSuchAlgorithmException, SignatureException,
            InvalidKeyException {
        while (true) {
            System.out.print("> ");

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
                case "cur" -> {
                    current();
                }
                case "del" -> {
                    delete();
                }
                case "echo" -> {
                    echo(theMsg);
                }
                case "gen" -> {
                    generateKey(theMsg);
                }
                case "help" -> {
                    help();
                }
                case "list" -> {
                    list();
                }
                case "peek" -> {
                    peek(theMsg);
                }
                case "use" -> {
                    useKey(theMsg);
                }
                default -> {}
            }
        }
    }

    private void help() {
        System.out.println(colorize("bye : exit the app.",
                new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT(), Attribute.BLACK_BACK())));

        System.out.println(
                colorize("cur : what is your current key document",
                        new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT(), Attribute.BLACK_BACK())) +
                colorize("`cur`", new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(), Attribute.BLACK_BACK()))
        );

        System.out.println(
                colorize(
                        "del : deletes all your key documents. be careful!",
                        new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT(),
                                Attribute.BLACK_BACK())
                ) +
                colorize("`del`", new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(), Attribute.BLACK_BACK()))
        );

        System.out.println(
                colorize("echo : prints user input.", new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT(),
                        Attribute.BLACK_BACK())) +
                        colorize("`echo <any text string here>`",
                                new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(), Attribute.BLACK_BACK()))
        );

        System.out.println(
                colorize("gen : generate a new key document. message is optional.",
                        new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT(), Attribute.BLACK_BACK())) +
                        colorize("`gen [<message>]`", new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(),
                                Attribute.BLACK_BACK()))
        );

        System.out.println(colorize(
                "help : help information.",
                new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT(), Attribute.BLACK_BACK())
        ));

        System.out.println(
                colorize("list : list all key documents. sorted by most recent first",
                        new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT(), Attribute.BLACK_BACK())) +
                        colorize("`list`",
                                new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(), Attribute.BLACK_BACK()))
        );

        System.out.println(
                colorize("peek : view the details of a key document",
                        new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT(), Attribute.BLACK_BACK())) +
                        colorize("`peek <# from `list`>`",
                                new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(), Attribute.BLACK_BACK()))
        );

        System.out.println(
                colorize("use : desigante currently used key document.",
                        new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT(), Attribute.BLACK_BACK())) +
                        colorize("`use <# from `list`>`",
                                new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(), Attribute.BLACK_BACK()))
        );
    }

    private void delete() throws IOException {
        Files.list(home).forEach(f -> {
            try {
                Files.delete(f);
                System.out.println(Txt.C.BRIGHT_YELLOW.get() + "removed `" + f.getFileName().toString() + "`"
                        + Txt.C.RESET.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println(colorize("complete.",
                new AnsiFormat(Attribute.BRIGHT_MAGENTA_TEXT(), Attribute.BLACK_BACK())));

        currentDocumentHash = StringUtils.EMPTY;
    }

    private void current() throws IOException {
        if(currentDocumentHash.isEmpty()) {
            System.out.println(colorize("no key document is active. " +
                    "try `use #` command.",
                    new AnsiFormat(Attribute.BRIGHT_RED_TEXT(), Attribute.BLACK_BACK())));
        } else {
            System.out.println(colorize(String.format("your active key document is `%s`", currentDocumentHash),
                    new AnsiFormat(Attribute.BRIGHT_MAGENTA_TEXT(), Attribute.BLACK_BACK())));

            Path p = Path.of(home.toString(), currentDocumentHash);
            String keyDoc = Files.readString(p);
            JSONObject doc = new JSONObject(keyDoc);
        }
    }

    private void peek(String msg) throws IOException {
        String[] arr = StringUtils.split(msg, StringUtils.SPACE);

        // no empties, plz
        if(msg.isEmpty()) return;

        if(arr.length <= 0) return;

        if(!StringUtils.isNumeric(arr[0].strip())) {
            System.out.println(colorize("not a number! =/",
                            new AnsiFormat(Attribute.BRIGHT_RED_TEXT(), Attribute.BLACK_BACK())));

            return;
        }

        int selection = Integer.parseInt(arr[0].strip());

        hashList = Files
                .list(home)
                .sorted((f1, f2) -> Long.valueOf(f2.toFile().lastModified())
                        .compareTo(f1.toFile().lastModified()))
                .toList();

        if(selection > hashList.size()-1 || selection < 0) {
            System.out.println(colorize("not a valid selection =(",
                    new AnsiFormat(Attribute.BRIGHT_RED_TEXT(), Attribute.BLACK_BACK())));

            return;
        }

        Path g = hashList.get(selection);
        currentDocumentHash = g.getFileName().toString();

        System.out.println(colorize("peeking at key document `" + currentDocumentHash + "`",
                new AnsiFormat(Attribute.BLUE_TEXT(), Attribute.BLACK_BACK())));

        String content = Files.readString(Path.of(home.toString(), currentDocumentHash));
        JSONObject doc = new JSONObject(content);
        //System.out.println(doc.toString(4));

        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("message", doc.getString("msg"));
        at.addRule();
        AT_Row timeRow = at.addRow(
                "timestamp",
                Instant.ofEpochMilli(doc.getLong("time"))
                        .atZone(ZoneId.of("UTC")).toLocalDateTime().toString()
        );
        timeRow.getCells().get(1).getContext().setTextAlignment(TextAlignment.CENTER);
        at.addRule();
        AT_Row factorRow = at.addRow(
                "work factor", String.valueOf(doc.getJSONObject("pow").getLong("work")));
        factorRow.getCells().get(1).getContext().setTextAlignment(TextAlignment.CENTER);
        at.addRule();
        AT_Row workHashRow = at.addRow("work hash", doc.getJSONObject("pow").getString("hash"));
        at.addRule();

        at.getRenderer().setCWC(new CWC_LongestWord());
        at.setPaddingLeftRight(2);

        System.out.println(colorize(at.render(),
                new AnsiFormat(Attribute.GREEN_TEXT(), Attribute.BLACK_BACK())));
    }

    private void useKey(String msg) throws IOException, ArrayIndexOutOfBoundsException {
        String[] arr = StringUtils.split(msg, StringUtils.SPACE);

        // no empties, plz
        if(msg.isEmpty()) {
            if(!currentDocumentHash.isEmpty()) {
                System.out.println(
                        colorize(String.format("current key document is set to `%s`", currentDocumentHash),
                                new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(), Attribute.BLACK_BACK())));
            } else {
                System.out.println(
                        colorize("no current key doc selected. select a key " +
                                "document (`use n` command). use `list` to show selections.",
                                new AnsiFormat(Attribute.BRIGHT_RED_TEXT(), Attribute.BLACK_BACK())));
            }

            return;
        }

        // if no first string is provided, user is querying current key document usage
        if(arr.length <= 0) return;

        // no alphas
        if(!StringUtils.isNumeric(arr[0].strip())) {
            System.out.println(colorize("not a number =/",
                            new AnsiFormat(Attribute.BRIGHT_RED_TEXT(), Attribute.BLACK_BACK())));
            return;
        }

        int selection = Integer.parseInt(arr[0].strip());

        hashList = Files.list(home)
                .sorted((f1, f2) -> Long.valueOf(f2.toFile().lastModified())
                        .compareTo(f1.toFile().lastModified()))
                .toList();

        if(selection > hashList.size()-1 || selection < 0) {
            System.out.println(colorize("not a valid selection =(",
                    new AnsiFormat(Attribute.BRIGHT_RED_TEXT(), Attribute.BLACK_BACK())));
        } else {
            Path g = hashList.get(selection);
            currentDocumentHash = g.getFileName().toString();

            System.out.println(colorize(
                    String.format("current key document is set to `%s`", currentDocumentHash),
                    new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(), Attribute.BLACK_BACK())));
        }
    }

    private void sayGoodbye() {
        System.out.println(colorize("exiting...",
                new AnsiFormat(Attribute.BRIGHT_RED_TEXT(), Attribute.BLACK_BACK())));
        scan.close();
        System.exit(0);
    }

    private void list() throws IOException {
        hashList = Files
                .list(home)
                .sorted((f1, f2) -> Long.valueOf(f2.toFile().lastModified())
                        .compareTo(f1.toFile().lastModified()))
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

            System.out.println(colorize(s,
                    new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(), Attribute.BLACK_BACK())));
        });

        if(!currentDocumentHash.isEmpty()) {
            System.out.println(colorize(String.format("current key document: `%s`", currentDocumentHash),
                    new AnsiFormat(Attribute.BRIGHT_RED_TEXT(), Attribute.BLACK_BACK())));
        }
    }

    /**
     * @param msg Generic string
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws IOException
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
        System.out.println(colorize("doing the work...",
                new AnsiFormat(Attribute.BRIGHT_MAGENTA_TEXT(), Attribute.BLACK_BACK())));

        JSONObject powJ = new JSONObject();
        powJ.put("hash", pow.getHash());
        powJ.put("work", pow.getNonce());
        keysJson.put("pow", powJ);

        System.out.println(colorize(String.format("work completed by a factor of %d, requiring %d bit(s). " +
                "signature: `%s`",
                pow.getNonce(), pow.getBitsNeeded(), pow.getHash()),
                new AnsiFormat(Attribute.BRIGHT_CYAN_TEXT(), Attribute.BLACK_BACK())));

        Path storePath = Path.of(System.getProperty("user.home").toString(), ".handoff", hashStr);

        Files.createDirectories(storePath.getParent());
        Files.createFile(storePath);

        Files.write(storePath, keysJson.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

        System.out.println(colorize(String.format(
                "key saved to document `%s`",
                storePath.toString()
                ),
                new AnsiFormat(Attribute.BRIGHT_BLUE_TEXT(), Attribute.BLACK_BACK())));
    }

    private void echo(String msg) {
        if(!msg.isEmpty()) {
            System.out.println(String.format("ECHO: %s", msg.toUpperCase()));
        } else {
            System.out.println("SILENCE");
        }
    }
}
