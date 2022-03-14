package org.white5moke.handoff.doc;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path save(KeyDocument keyDoc) throws IOException {
        // write content to file
        Path filename = Files.createFile(Path.of(getPath().toString(), keyDoc.getHash()));
        Files.writeString(filename, keyDoc.toString());

        return filename;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public void setCurrentHash(String currentHash) {
        this.currentHash = currentHash;
    }
}
