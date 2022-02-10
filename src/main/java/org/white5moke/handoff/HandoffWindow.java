package org.white5moke.handoff;

import org.json.JSONObject;
import org.white5moke.handoff.document.KeyDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.concurrent.atomic.AtomicInteger;

public class HandoffWindow extends JFrame {
    private final Container basePanel;
    private Handoff handoff;
    private JButton newKeyButton;

    public HandoffWindow() {
        super("handoff");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setSize(new Dimension(1000, 640));
        basePanel = new JPanel();
        basePanel.setLayout(new BorderLayout());
        setContentPane(basePanel);

        handoff = new Handoff();

        buildUI();

        listeners();

        setVisible(true);
    }

    private void listeners() {
        newKeyButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    KeyDocument keyDoc = handoff.generateKeyDocument();

                } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException
                        | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void buildUI() {
        JScrollPane keyDocScroller = new JScrollPane();
        buildKeyDocsList();
        getContentPane().add(BorderLayout.CENTER, keyDocScroller);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 1, 5, 5));
        newKeyButton = new JButton("new key");
        buttonPanel.add(newKeyButton);

        getContentPane().add(BorderLayout.SOUTH, buttonPanel);
    }

    private void buildKeyDocsList() {
        AtomicInteger i = new AtomicInteger(0);
        handoff.getFileList().forEach(f -> {
            try {
                handoff.fromFile(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
