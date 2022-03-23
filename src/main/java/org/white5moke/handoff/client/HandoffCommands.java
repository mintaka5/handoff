package org.white5moke.handoff.client;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.white5moke.handoff.SignThis;
import org.white5moke.handoff.doc.KeyDocument;
import org.white5moke.handoff.doc.SigningDocument;
import org.white5moke.handoff.doc.TheStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class HandoffCommands {
    private Scanner scan;
    private TheStore store;
    private SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");

    public HandoffCommands(TheStore store, Scanner scan) {
        setScan(scan);
        setStore(store);
    }

    public void generateDocument(String msg) {
        msg = msg.strip();

        KeyDocument keyDoc = new KeyDocument();
        keyDoc.generate(msg, 256, 2048);

        try {
            Path save = getStore().save(keyDoc);
            System.out.printf("<< key document saved `%s`%n", save.toString());
        } catch (IOException e) {
            System.err.printf("<x could not save key document: `%s`%n", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void hashIt(String msg) {
        msg = msg.strip();

        if (!msg.isEmpty())
            System.out.printf("<< hash of the message `%s`:%n%s%n",
                    msg.strip(),
                    DigestUtils.sha256Hex(msg.getBytes(StandardCharsets.UTF_8))
            );
    }

    public void listEm(TheStore store) {
        try {
            AtomicInteger i = new AtomicInteger(1);
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");

            Files.list(store.getPath())
                    .sorted((a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified()))
                    .forEach((filename) -> {
                        System.out.printf(
                                "<< %d) key doc: `%s` @ %s%n",
                                i.getAndIncrement(),
                                filename.getFileName().toString(),
                                formatter.format(Date.from(Instant.ofEpochMilli(filename.toFile().lastModified())))
                        );
                    });
        } catch (IOException e) {
            System.err.printf("<x could not retrieve listing of path: `%s`%n", e.getMessage());
        }
    }

    public void useIt(String msg) {
        msg = msg.strip();
        if (StringUtils.isNumeric(msg)) {
            int sel = Integer.parseInt(msg.strip());
            sel = sel - 1; // because we're listing +1 offset

            try {
                List<Path> docList = Files.list(getStore().getPath()).sorted(
                        (a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified())
                ).toList();
                int docListSize = docList.size();

                // make sure we're in range
                if (sel >= 0 && sel < docListSize) {
                    Path docPath = docList.get(sel);
                    getStore().setCurrentHash(docPath.getFileName().toString());
                    System.out.printf("<< current key document is set to `%s`%n", getStore().getCurrentHash());
                } else { // out of range
                    System.out.printf("<x %d is outside of the range of %d key doc(s)%n", sel + 1, docListSize);
                    // list em
                    AtomicInteger i = new AtomicInteger(1);
                    Files.list(this.store.getPath())
                            .sorted((a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified()))
                            .forEach((filename) -> {
                                System.out.printf(
                                        "<< %d) key doc: `%s` @ %s%n",
                                        i.getAndIncrement(),
                                        filename.getFileName().toString(),
                                        formatter.format(Date.from(Instant.ofEpochMilli(filename.toFile().lastModified())))
                                );
                            });
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.printf("<x `%s` is not a number. NaN!%n", msg);
        }
    }

    public void helpMe(String msg) {
        msg = msg.strip();
        String what = "";
        if (!msg.isEmpty()) {
            String[] stuff = StringUtils.split(msg, StringUtils.SPACE);
            String cmd = stuff[0].strip();

            // TODO : individual command help
        } else {
            // list all
            // bye | exit | quit
            System.out.printf("<< %1$-34s: close application.%n", "bye | exit | quit");
            // cur | current
            System.out.printf("<< %1$-34s: what is the current document being used?%n", "cur | current");
            // gen | generate
            System.out.printf("<< %1$-34s: generate a new key document. add text after command, to include message.%n",
                    "gen | generate <string>?");
            // hash
            System.out.printf("<< %1$-34s: provide a text string, get a sha256 hash of it.%n", "hash <string>");
            // help
            System.out.printf("<< %1$-34s: list all available commands.%n", "help");
            // ls | list | keys
            System.out.printf("<< %1$-34s: get a list of all your key documents.%n", "ls | list | keys");
            // sign
            System.out.printf("<< %1$-34s: sign any given message with currently active key document%n",
                    "sign <random message>");
            // use | select | pick
            System.out.printf(
                    "<< %1$-34s: set the default/current document to be used for things like signing or encrypting.%n",
                    "use | select | pick <num>");
            // verify
            System.out.printf("<< %1$-34s: verify a signed message%n",
                    "verify <orig msg> <sig> <pub key>");
            // view | peek | show | deets
            System.out.printf("<< %1$-34s: provides some more details about the document.%n",
                    "view | peek | show | deets <num>");
        }
    }

    public void currentDoc() {
        Path file = Path.of(getStore().getPath().toString(), getStore().getCurrentHash());
        System.out.printf("<< current key doc hash: `%s` @ %s%n",
                getStore().getCurrentHash(),
                formatter.format(Date.from(Instant.ofEpochMilli(file.toFile().lastModified())))
        );
    }

    public void deets(String msg) {
        msg = msg.strip();
        if (!msg.isEmpty()) {
            String[] stuff = StringUtils.split(msg, StringUtils.SPACE);
            String sel = stuff[0].strip();
            if (StringUtils.isNumeric(sel)) {
                int selection = Integer.parseInt(sel) - 1;

                List<Path> docList = null;
                try {
                    docList = Files.list(getStore().getPath()).sorted(
                            (a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified())
                    ).toList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int docListSize = docList.size();
                System.out.printf("<< list size: %d :: sel: %s :: selection: %d%n",
                        docListSize, sel, selection
                );
                if (selection >= 0 && selection < docListSize) {
                    Path docFile = docList.get(selection);

                    if (docFile.toFile().exists()) {
                        try {
                            KeyDocument doc = TheStore.docToKeyDocument(docFile);
                            System.out.println("<< hash: " + doc.getHash());
                            System.out.println("<< timestamp: " + formatter.format(
                                    Date.from(Instant.ofEpochMilli(doc.getTimestamp())))
                            );
                            System.out.println("<< message: " + doc.getMessage());
                            System.out.printf("<< public signing key: %s%n",
                                    Base64.getEncoder()
                                            .encodeToString(doc.getSigning().getKeyPair().getPublic().getEncoded())
                            );
                            System.out.printf("<< public encryption key: %s%n",
                                    Base64.getEncoder()
                                            .encodeToString(doc.getEncrypting().getKeyPair().getPublic().getEncoded())
                            );

                            System.out.println(String.format("<< signature: %s",
                                    Base64.getEncoder().encodeToString(doc.getSignature())));
                        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("<< it seems your key document went missing. missing key document.");
                    }
                } else {
                    System.err.println("<< your selection is beyond the range of the document list.");
                }
            } else {
                System.err.println("<< NaN. use `ls` or `list`");
            }
        } else {
            System.err.println("<< please, provide a number from the list command. `ls` or `list`");
        }
    }

    public void setScan(Scanner scan) {
        this.scan = scan;
    }

    public TheStore getStore() {
        return store;
    }

    public void setStore(TheStore store) {
        this.store = store;
    }

    public void four0Four() {
        System.err.println("<< error 0x4 0x0 0x4. all your base belong to us!");
    }

    public void signIt(String msg) {
        msg = msg.strip();

        System.out.printf("<< using key document: `%s` to sign the message `%s`%n",
                getStore().getCurrentHash(),
                msg
                );

        Path filename = Path.of(getStore().getPath().toString(), getStore().getCurrentHash());
        if(Files.exists(filename)) {
            // get the signing private key
            try {
                KeyDocument doc = TheStore.docToKeyDocument(filename);
                KeyPair signPair = doc.getSigning().getKeyPair();

                byte[] signature = SignThis.sign(msg.getBytes(StandardCharsets.UTF_8), signPair.getPrivate());
                String pubS = Base64.getEncoder().encodeToString(signPair.getPublic().getEncoded());

                System.out.printf("<< signature: %s%n<< public key (for verification): %s%n",
                        Base64.getEncoder().encodeToString(signature),
                        Base64.getEncoder().encodeToString(signPair.getPublic().getEncoded())
                        );
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException |
                    SignatureException e) {
                e.printStackTrace();
            }
        }
    }

    public void verifyIt(String theMsg) {
        theMsg = theMsg.strip();

        String origMsg;
        String signedMsg;
        String publicKey;

        if(!theMsg.isEmpty()) {
            String[] splitUp = StringUtils.split(theMsg, StringUtils.SPACE);

            if(splitUp.length == 3) {
                origMsg = splitUp[0].strip();
                signedMsg = splitUp[1].strip();
                publicKey = splitUp[2].strip();

                byte[] signedMsgBs = Base64.getDecoder().decode(signedMsg);

                boolean isVerified = false;
                try {
                    isVerified = SignThis.isValidSignature(
                            origMsg.getBytes(StandardCharsets.UTF_8),
                            SigningDocument.pubKeyFromBytes(Base64.getDecoder().decode(publicKey)),
                            signedMsgBs
                    );

                    if(isVerified) {
                        System.out.println("<< message signature is valid");
                    } else {
                        System.err.println("<< message signature cannot be verified");
                    }
                } catch (NoSuchAlgorithmException e) {
                    System.err.println("<< invalid signature algorithm was supplied. key could be corrupted.");
                } catch (InvalidKeyException | InvalidKeySpecException e) {
                    System.err.println("<< invalid public key was provided.");
                } catch (SignatureException e) {
                    System.err.println("<< message signature is invalid.");
                }
            } else {
                System.err.println("<< 3 arguments required 1) original message, 2) signature, and 3) public key.");
            }
        } else {
            System.err.println("<< please provide 1) original message, 2) signature, and 3) public key.");
        }
    }
}
