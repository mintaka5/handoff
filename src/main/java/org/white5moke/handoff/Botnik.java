package org.white5moke.handoff;

import org.apache.commons.codec.digest.DigestUtils;

import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.font.OpenType;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Scanner;

public class Botnik implements Runnable {
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
        System.out.print(">> ");

        while(scanner.hasNext() && !scanner.next().equals("exit")) {
            Instant now = Instant.now();
            String tag = Utilities.randomID(4);

            // operate
            String input = scanner.next().strip();
            String inHash = DigestUtils.sha256Hex(input.getBytes(StandardCharsets.UTF_8)); // hash it for authenticity
            try {
                store(Instant.now(), inHash, input);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.print(">> ");
        }
        System.out.println("bye x.x");
        System.exit(0);
    }

    private void store(Instant now, String inHash, String input) throws IOException {
        ByteBuffer nowBB = ByteBuffer.allocate(Long.BYTES+1);
        ByteBuffer hashBB = ByteBuffer.allocate(64+1);
        ByteBuffer inBB = ByteBuffer.allocate(input.length()+1);

        nowBB.putLong(now.toEpochMilli());
        hashBB.put(DigestUtils.sha256(input));
        inBB.put(input.getBytes(StandardCharsets.UTF_8));

        ByteBuffer rowBB = ByteBuffer.allocate(nowBB.capacity() + hashBB.capacity() + inBB.capacity());
        rowBB.put(nowBB.array()).put(hashBB.array()).put(inBB.array());

        FileOutputStream io = new FileOutputStream(storagePath.toFile(), true);
        io.write(rowBB.array());
        io.close();
    }

    private void respond(String input) {

    }

    public static void main(String[] args) {
        Thread botThread = new Thread(new Botnik(Path.of(System.getProperty("user.home"), ".botnik", "stowage")));
        botThread.start();
    }
}

