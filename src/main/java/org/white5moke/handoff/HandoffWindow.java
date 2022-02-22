package org.white5moke.handoff;

import org.apache.commons.lang3.RandomUtils;
import org.json.JSONObject;
import org.white5moke.handoff.document.KeyDocument;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
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
                    // make sure signature is a-ok, to add it to key doc list!
                    if(doc.isSignatureOk()) keyDocuments.add(doc);
                } catch (IOException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
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

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 1, 5, 5));
        newKeyButton = new JButton("new key");
        buttonPanel.add(newKeyButton);

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.GREEN);
        centerPanel.setLayout(new CardLayout());

        JPanel docListPanel = new JPanel();
        docListPanel.setLayout(new GridLayout(getKeyDocuments().size(), 1));
        getKeyDocuments().forEach(d -> {
            // add stuff to doc list
            JPanel docPanel = new JPanel();
            docPanel.setBorder(new CompoundBorder(
                    new EmptyBorder(10, 10, 10, 10),
                    new LineBorder(Color.GRAY, 1, true)
                    )
            );
            docPanel.setLayout(new GridBagLayout());
            docPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    e.getComponent().setBackground(Color.GRAY);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    e.getComponent().setBackground(UIManager.getColor("Panel.background"));
                }
            });
            JLabel hashLabel = new JLabel(d.getHash());
            GridBagConstraints gcHashLabel = new GridBagConstraints();
            gcHashLabel.fill = GridBagConstraints.BOTH;
            gcHashLabel.insets = new Insets(0, 0, 5, 5);
            gcHashLabel.gridx = 0;
            gcHashLabel.gridy = 0;
            docPanel.add(hashLabel, gcHashLabel);
            docListPanel.add(docPanel);
        });
        JScrollPane docScroll = new JScrollPane(docListPanel);
        docScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        docScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerPanel.add(docScroll);

        getContentPane().add(BorderLayout.NORTH, top);
        getContentPane().add(BorderLayout.CENTER, centerPanel);
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
