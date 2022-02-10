package org.white5moke.handoff;

import org.json.JSONObject;
import org.white5moke.handoff.document.KeyDocument;

import java.awt.font.OpenType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

public class Handoff {
    private Path homeDir;
    private List<Path> fileList;

    public Handoff() {
        homeDir = Path.of(System.getProperty("user.home"), ".handoff");
        try {
            Files.createDirectories(homeDir.getParent());
            Files.createDirectory(homeDir);
        } catch (IOException e) {
            System.out.println(e.getMessage() + " : failed to create home directories for storage");
        }

        fileList = new ArrayList<>();
        try {
            populateFileList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Path> populateFileList() throws IOException {
        fileList = Files.list(homeDir).sorted(
                (a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified())
        ).toList();

        return fileList;
    }

    public KeyDocument generateKeyDocument() throws NoSuchAlgorithmException, SignatureException,
            InvalidKeyException, IOException {
        KeyDocument doc = new KeyDocument();
        toFile(doc, homeDir);
        populateFileList();

        return doc;
    }

    private Path toFile(KeyDocument doc, Path dir) throws NoSuchAlgorithmException, SignatureException,
            InvalidKeyException, IOException {
        byte[] docBs = doc.aggregateBytes();
        String filename = new String(doc.getHash(), StandardCharsets.UTF_8);

        Path newDoc = Files.createFile(Path.of(dir.toString(), filename.trim()));
        newDoc = Files.write(newDoc, docBs, StandardOpenOption.CREATE_NEW);

        return newDoc;
    }

    public List<Path> getFileList() {
        return fileList;
    }

    public KeyDocument fromFile(Path path) throws IOException {
        byte[] jsonBs = Files.readAllBytes(path);
        String jsonS = new String(jsonBs, StandardCharsets.UTF_8);

        JSONObject j = new JSONObject(jsonS);

        return new KeyDocument(j);
    }
}
