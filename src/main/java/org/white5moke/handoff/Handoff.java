package org.white5moke.handoff;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.white5moke.handoff.document.KeyDocument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Handoff {
    private Path homeDir;
    private List<Path> fileList;

    public Handoff() {
        homeDir = Path.of(System.getProperty("user.home"), ".handoff");
        try {
            Files.createDirectories(homeDir.getParent());
            Files.createDirectory(homeDir);
        } catch (FileAlreadyExistsException e1) {
            System.out.println("key document directory already exists...move along...");
        } catch (IOException e) {
            e.printStackTrace();
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
            InvalidKeyException {
        byte[] docBs = doc.aggregateBytes();
        String filename = Hex.encodeHexString(doc.getHash());

        Path newDoc = null;

        try {
            newDoc = Files.write(Path.of(homeDir.toString(), filename), docBs, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

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
