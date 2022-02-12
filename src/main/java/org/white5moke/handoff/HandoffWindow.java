package org.white5moke.handoff;

import org.json.JSONObject;
import org.white5moke.handoff.document.KeyDocument;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HandoffWindow extends JFrame {
    private final Container basePanel;
    private Handoff handoff;
    private JButton newKeyButton;
    private JTextField messageText;
    private List<KeyDocument> keyDocuments = new ArrayList<>();

    public HandoffWindow() {
        super("handoff");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setSize(new Dimension(1000, 640));
        basePanel = new JPanel();
        basePanel.setLayout(new BorderLayout());
        setContentPane(basePanel);

        handoff = new Handoff();

        updateKeyDocuments();

        buildUI();

        listeners();

        setVisible(true);
    }

    private void updateKeyDocuments() {
        try {
            Files.list(handoff.getHomeDirectory()).forEach(path -> {
                try {
                    KeyDocument doc = handoff.fromFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listeners() {
        newKeyButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handoff.create(messageText.getText());
                messageText.setText("");
                updateKeyDocuments();
            }
        });
    }

    private void buildUI() {
        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        JLabel tempLbl = new JLabel("Hi there!");
        center.add(BorderLayout.CENTER, tempLbl);
        getContentPane().add(BorderLayout.CENTER, center);

        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        JPanel messagePanel = new JPanel();
        messagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagLayout messageLayout = new GridBagLayout();
        messageLayout.columnWidths = new int[] {86, 86, 0};
        messageLayout.rowHeights = new int[] {20, 20, 20, 20, 20, 0};
        messageLayout.columnWeights = new double[] {0, 1, Double.MIN_VALUE};
        messageLayout.rowWeights = new double[] {0, 0, 0, 0, 0, Double.MIN_VALUE};
        messagePanel.setLayout(messageLayout);

        messageText = buildTextField("message", 0, messagePanel);
        top.add(BorderLayout.CENTER, messagePanel);

        getContentPane().add(BorderLayout.NORTH, top);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 1, 5, 5));
        newKeyButton = new JButton("new key");
        buttonPanel.add(newKeyButton);

        getContentPane().add(BorderLayout.SOUTH, buttonPanel);
    }

    private JTextField buildTextField(String label, int position, JPanel containerPanel) {
        JLabel lbl = new JLabel(label);
        GridBagConstraints gcLabel = new GridBagConstraints();
        gcLabel.fill = GridBagConstraints.BOTH;
        gcLabel.insets = new Insets(0, 0, 5, 5);
        gcLabel.gridx = 0;
        gcLabel.gridy = position;
        containerPanel.add(lbl, gcLabel);

        JTextField txt = new JTextField();
        GridBagConstraints gcTxt = new GridBagConstraints();
        gcTxt.fill = GridBagConstraints.BOTH;
        gcTxt.insets = new Insets(0, 0, 5, 0);
        gcTxt.gridx = 1;
        gcTxt.gridy = position;
        containerPanel.add(txt, gcTxt);
        txt.setColumns(10);

        return txt;
    }

    public List<KeyDocument> getKeyDocuments() {
        return keyDocuments;
    }
}
