package org.white5moke.handoff;

import org.white5moke.handoff.document.KeyDocument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Handoff {
    private Path homeDirectory;
    private List<KeyDocument> keyDocumentList = new ArrayList<>();

    public Handoff() {
        // set up storage directory
        Path dir = Path.of(System.getProperty("user.home"), ".handoff");
        setHomeDirectory(dir);

        try {
            Files.createDirectories(getHomeDirectory().getParent());
            Files.createDirectory(dir);
        } catch (IOException e) {
            System.out.println("problem creating top level directories: " + e.getMessage());
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
        toFile(kDoc);

        return kDoc;
    }

    private void toFile(KeyDocument kDoc) {
        System.out.println(kDoc);
    }

    public List<KeyDocument> getDocuments() throws IOException {
        List<KeyDocument> l = new ArrayList<>();

        Files.list(getHomeDirectory()).forEach(f -> {
            try {
                KeyDocument kDoc = fromFile(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return l;
    }

    private KeyDocument fromFile(Path f) throws IOException {
        byte[] file = Files.readAllBytes(f);


        return null;
    }
}
