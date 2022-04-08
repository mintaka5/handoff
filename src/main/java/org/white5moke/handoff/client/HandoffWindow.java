package org.white5moke.handoff.client;

import dan.johnson.GridConstraints;
import org.apache.commons.lang3.time.DateUtils;
import org.white5moke.handoff.doc.KeyDocument;
import org.white5moke.handoff.doc.TheStore;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class HandoffWindow extends JFrame {
    private TheStore theStore;
    private JPanel detailsPanel;

    public HandoffWindow(String title, int w, int h) {
        super(title);
        setSize(new Dimension(w, h));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);

        theStore = new TheStore(Path.of(System.getProperty("user.home"), ".handoff"));

        try {
            draw();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    private void draw() throws IOException {
        drawContent();
    }

    private void drawContent() throws IOException {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // details panel
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new CardLayout());

        JPanel docsList = new JPanel();
        docsList.setLayout(new BoxLayout(docsList, BoxLayout.PAGE_AXIS));
        docsList.setBackground(Color.BLACK);
        add(docsList, BorderLayout.EAST);

        AtomicInteger i = new AtomicInteger(0);
        Files.list(theStore.getPath())
                .sorted((a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified()))
                .forEach(f -> {
            System.out.println(f.toString());
            try {
                KeyDocument doc = TheStore.docToKeyDocument(f);
                String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochMilli(doc.getTimestamp()));

                JPanel docPanel = new JPanel();
                docPanel.setLayout(new GridBagLayout());
                GridConstraints gbc1 = new GridConstraints();
                docPanel.setBackground(Color.WHITE);
                docsList.add(docPanel);

                gbc1.anchor = GridConstraints.FIRST_LINE_START;
                gbc1.insets = new Insets(5, 5, 5, 5);
                gbc1.gridx = 0;
                gbc1.gridy = 0;
                JLabel lblTime = new JLabel(timestamp);
                docPanel.add(lblTime, gbc1);

                gbc1.gridx = 1;
                gbc1.gridy = 0;
                JLabel lblTag = new JLabel(doc.getTag());
                docPanel.add(lblTag, gbc1);

                gbc1.gridx = 0;
                gbc1.gridy = 1;
                gbc1.gridwidth = 2;
                gbc1.fill = GridConstraints.HORIZONTAL;
                JLabel lblHash = new JLabel(doc.getHash());
                docPanel.add(lblHash, gbc1);

                gbc1.gridx = 0;
                gbc1.gridy = 2;
                JPanel btnPanel = new JPanel();
                JButton btnDetails = new JButton("details...");
                btnPanel.add(btnDetails);
                JButton btnDel = new JButton("delete");
                btnPanel.add(btnDel);
                docPanel.add(btnPanel, gbc1);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        });

        Path mostRecent = Files.list(theStore.getPath())
                .min((a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified()))
                .get();
        try {
            KeyDocument mostRecentDoc = TheStore.docToKeyDocument(mostRecent);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new HandoffWindow("handoff", 480, 540);
    }
}
