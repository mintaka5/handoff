package org.white5moke.handoff.doc;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Base64;

public class TheStore {
    private Path path;

    private String currentHash = StringUtils.EMPTY;

    public TheStore(Path dir) {
        setPath(dir);
        init();
    }

    private void init() {
        // create parent directories if they don't exist
        try {
            Files.createDirectories(getPath().getParent());
        } catch (IOException e) {
            System.out.println(">> cannot create user home directory");
        }
    }

    public JSONObject docToJson(Path filename) {
        String content = "";
        JSONObject json = null;
        try {
            content = Files.readString(filename);
            // unpack base64 encoding and convert back into json data
            // TODO : need to encrypt this with generated key and connect them somehow????
            byte[] contentBs = Base64.getMimeDecoder().decode(content);
            String contentS = new String(contentBs, StandardCharsets.UTF_8);
            json = new JSONObject(contentS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    public KeyDocument docToKeyDocument(Path filename) throws NoSuchAlgorithmException, InvalidKeySpecException {
        JSONObject json = docToJson(filename);
        //System.out.println(json.toString(4));
        KeyDocument doc = null;
        if(json != null) {
            doc = KeyDocument.fromJson(json);
        }

        return doc;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path save(KeyDocument keyDoc) throws IOException, SQLException {
        // write content to file
        Path filename = Files.createFile(Path.of(getPath().toString(), keyDoc.getHash()));

        byte[] keyDocBs = keyDoc.toString().getBytes(StandardCharsets.UTF_8);
        String keyDocB64 = Base64.getMimeEncoder().encodeToString(keyDocBs);
        Files.writeString(filename, keyDocB64);

        return filename;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public void setCurrentHash(String currentHash) {
        this.currentHash = currentHash;
    }
}
