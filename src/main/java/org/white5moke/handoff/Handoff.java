package org.white5moke.handoff;

import org.json.JSONObject;
import org.white5moke.handoff.document.KeyDocument;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Handoff {
    private Path homeDirectory;

    public Handoff() {
        // set up storage directory
        Path dir = Path.of(System.getProperty("user.home"), ".handoff");
        setHomeDirectory(dir);

        try {
            Files.createDirectories(getHomeDirectory().getParent());
            Files.createDirectory(dir);
        } catch (FileAlreadyExistsException fe) {
            System.out.printf("user directory, %s exists%n", fe.getMessage());
        } catch (IOException e) {
            System.out.printf("i/o error: %s%n", e.getMessage());
        }


    }

    private void setHomeDirectory(Path dir) {
        this.homeDirectory = dir;
    }

    public Path getHomeDirectory() {
        return homeDirectory;
    }

    public KeyDocument create(String message) {
        KeyDocument kDoc = new KeyDocument();
        kDoc.generate(message);
        JSONObject j = new JSONObject(kDoc.toString());
        toFile(kDoc);

        return kDoc;
    }

    private void toFile(KeyDocument kDoc) {;
        String filename = kDoc.getHash();
        try {
            Files.writeString(
                    Path.of(getHomeDirectory().toString(), filename),
                    kDoc.toString(),
                    StandardOpenOption.CREATE_NEW
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public KeyDocument fromFile(Path path) throws IOException {
        KeyDocument doc = new KeyDocument();
        String jsonS = Files.readString(path);
        JSONObject json = new JSONObject(jsonS);

        doc.fromJson(json);

        return doc;
    }
}
