package org.white5moke.handoff;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.ByteUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Scanner;

public class Botnik implements Runnable {
    private static final int HASH_BYTE_LEN = 64;
    private static final int MSG_BYTE_LEN = 1024;
    private static final int TOTAL_BYTE_LEN = Long.BYTES + HASH_BYTE_LEN + MSG_BYTE_LEN;
    private final Scanner scanner = new Scanner(System.in);
    private Path storagePath;

    public Botnik(Path path) {
        storagePath = path;

        if(!Files.exists(storagePath.getParent())) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(storagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        pullAll();
        while(scanner.hasNext()) {
            Instant now = Instant.now();
            String tag = Utilities.randomID(4);

            // operate
            String input = scanner.next().strip();
            String inHash = DigestUtils.sha256Hex(input.getBytes(StandardCharsets.UTF_8)); // hash it for authenticity
            try {
                store(Instant.now(), inHash, input);

                //Trainer training = train(input);
                //respond(training);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("bye x.x");
        System.exit(0);
    }

    private void pullAll() {
        try {
            InputStream io = Files.newInputStream(storagePath);
            byte[] b = io.readAllBytes();
            io.close();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Trainer train(String input) {
        Trainer train = new Trainer();
        train.pipe(input);

        return train;
    }

    private void respond(Trainer training) {
        try {
            InputStream io = Files.newInputStream(storagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void store(Instant now, String inHash, String input) throws IOException {
        byte[] timeBs = ByteBuffer.allocate(Long.BYTES).putLong(Instant.now().toEpochMilli()).array();
        byte[] hashBs = inHash.getBytes(StandardCharsets.UTF_8);
        byte[] msgBs = input.strip().getBytes(StandardCharsets.UTF_8);

        ByteBuffer bb = ByteBuffer.allocate(TOTAL_BYTE_LEN);
        bb.put(timeBs).put(hashBs).put(msgBs);

        OutputStream oi = Files.newOutputStream(storagePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        oi.write(bb.array());
        oi.flush();
        oi.close();
    }

    public static void main(String[] args) {
        Thread botThread = new Thread(new Botnik(Path.of(System.getProperty("user.home"), ".botnik", "stowage")));
        botThread.start();
    }
}

class Trainer {
    public Trainer() {}

    public void pipe(String input) {
        
    }
}

