package org.white5moke.handoff;

import org.white5moke.handoff.document.KeyDocument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Handoff {
    private Path homeDir;
    private List<Path> fileList;

    public Handoff() {
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

    public KeyDocument generateKeyDocument() {
        KeyDocument doc = new KeyDocument();

        return doc;
    }
}
