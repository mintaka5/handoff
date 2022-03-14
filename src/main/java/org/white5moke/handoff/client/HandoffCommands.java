package org.white5moke.handoff.client;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.white5moke.handoff.doc.KeyDocument;
import org.white5moke.handoff.doc.TheStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
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
        KeyDocument keyDoc = new KeyDocument();
        keyDoc.generate(msg, 256, 2048);

        try {
            Path save = getStore().save(keyDoc);
            System.out.printf("<< key document saved `%s`%n", save.toString());
        } catch (IOException e) {
            System.out.printf("<x could not save key document: `%s`%n", e.getMessage());
        }
    }

    public void nothingToDo() {
        System.out.println("nothing to do.");
    }

    public void sayGoodbye() {
        System.out.println("<< exiting...");
        System.exit(0);
    }

    public void hashIt(String msg) {
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
            System.out.printf("<x could not retrieve listing of path: `%s`%n", e.getMessage());
        }
    }

    public void useIt(String msg) {
        msg = msg.strip();
        if(StringUtils.isNumeric(msg)) {
            int sel = Integer.parseInt(msg.strip());
            sel = sel - 1; // because we're listing +1 offset

            try {
                List<Path> docList = Files.list(getStore().getPath()).sorted(
                        (a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified())
                ).toList();
                int docListSize = docList.size();

                // make sure we're in range
                if(sel >= 0 && sel < docListSize) {
                    Path docPath = docList.get(sel);
                    getStore().setCurrentHash(docPath.getFileName().toString());
                    System.out.printf("<< current key document is set to `%s`%n", getStore().getCurrentHash());
                } else { // out of range
                    System.out.printf("<x %d is outside of the range of %d key doc(s)%n", sel+1, docListSize);
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

    public void setScan(Scanner scan) {
        this.scan = scan;
    }

    public Scanner getScan() {
        return scan;
    }

    public TheStore getStore() {
        return store;
    }

    public void setStore(TheStore store) {
        this.store = store;
    }
}