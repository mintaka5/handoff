package org.white5moke.handoff;

import org.white5moke.handoff.document.KeyDocument;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HandoffWindow extends JFrame {
    private final Container basePanel;
    private Handoff handoff;
    private JButton newKeyButton;
    private JTextField messageText;
    private List<KeyDocument> keyDocuments = new ArrayList<>();
    private JPanel docListPanel;
    private JLabel homeMsgText;
    private JLabel homeHashTxt;

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

    /**
     * updates list of key documents based off of
     * user's home path.
     */
    private void updateKeyDocuments() {
        try {
            // resort file list and order by modification date descending order
            Files.list(handoff.getHomeDirectory()).sorted((a, b) -> {
                return (int) (b.toFile().lastModified() - a.toFile().lastModified());
            }).forEach(path -> {
                try {
                    KeyDocument doc = handoff.fromFile(path);
                    // make sure signature is a-ok, to add it to key doc list!
                    if(doc.isSignatureOk()) keyDocuments.add(doc);
                } catch (IOException | NoSuchAlgorithmException | SignatureException |
                        InvalidKeyException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateDocList();
    }

    /**
     * a place to keep all GUI listeners for sanity's sake
     */
    private void listeners() {
        newKeyButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handoff.create(messageText.getText());
                messageText.setText("");
                updateKeyDocuments();
            }
        });

        messageText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(messageText.getText().length() > 1) {
                    newKeyButton.setEnabled(true);
                } else {
                    newKeyButton.setEnabled(false);
                }
            }
        });
    }

    /**
     * build out interface
     */
    private void buildUI() {
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        JPanel messagePanel = new JPanel();
        messagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagLayout messageLayout = new GridBagLayout();
        messageLayout.columnWidths = new int[] {100, 100, 0};
        messageLayout.rowHeights = new int[] {20, 20, 20, 20, 20, 0};
        messageLayout.columnWeights = new double[] {0, 1, Double.MIN_VALUE};
        messageLayout.rowWeights = new double[] {0, 0, 0, 0, 0, Double.MIN_VALUE};
        messagePanel.setLayout(messageLayout);

        messageText = buildTextField("message", 0, messagePanel);
        top.add(BorderLayout.CENTER, messagePanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 1, 5, 5));
        newKeyButton = new JButton("new key");
        newKeyButton.setEnabled(false);

        buttonPanel.add(newKeyButton);

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.GREEN);
        centerPanel.setLayout(new CardLayout());

        docListPanel = new JPanel();
        docListPanel.setLayout(new GridLayout(getKeyDocuments().size(), 1));
        updateKeyDocuments();

        JScrollPane docScroll = new JScrollPane(docListPanel);
        docScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        docScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerPanel.add(docScroll);

        // panel for SOUTH
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());
        JPanel southCenterPanel = new JPanel();
        southCenterPanel.setLayout(new CardLayout());

        JPanel homeCard = new JPanel();
        // set up home card
        GridBagLayout homeCardLayout = new GridBagLayout();
        homeCardLayout.columnWidths = new int[] {100, 100, 0};
        homeCardLayout.rowHeights = new int[] {20, 20, 20, 20, 20, 0};
        homeCardLayout.columnWeights = new double[] {0, 1, Double.MIN_VALUE};
        homeCardLayout.rowWeights = new double[] {0, 0, 0, 0, 0, Double.MIN_VALUE};
        messagePanel.setLayout(homeCardLayout);
        // get latest key doc file in list
        KeyDocument latestDoc = getKeyDocuments().stream().findFirst().orElse(null);
        JLabel homeMsgLbl = new JLabel("message:");
        homeCard.add(homeMsgLbl, gbc(0, 0, new int[] {0, 0, 5, 5}));
        homeMsgText = new JLabel(latestDoc.getMessage());
        homeCard.add(homeMsgText, gbc(1, 0, new int[] {0, 0, 5, 5}));
        JLabel homeHashLbl = new JLabel("hash:");
        homeHashTxt = new JLabel(latestDoc.getHash());
        homeCard.add(homeHashLbl, gbc(0, 1, new int[] {0, 0, 5, 5}));
        homeCard.add(homeHashTxt, gbc(1, 1, new int[] {0, 0, 5, 5}));

        JPanel detailCard = new JPanel();
        southCenterPanel.add(homeCard, 0);
        southCenterPanel.add(detailCard, 1);
        southPanel.add(southCenterPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(BorderLayout.NORTH, top);
        getContentPane().add(BorderLayout.CENTER, centerPanel);
        getContentPane().add(BorderLayout.SOUTH, southPanel);
    }

    public GridBagConstraints gbc(int gridX, int gridY, int[] insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(insets[0], insets[1], insets[2], insets[3]);
        gbc.gridx = gridX;
        gbc.gridy = gridY;

        return gbc;
    }

    /**
     * update the GUI list items
     */
    private void updateDocList() {
        docListPanel.removeAll();

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
                public void mouseReleased(MouseEvent e) {
                    System.out.print("DEBUG :: mouse event: signature - " + d.getSignature());
                    System.out.print(" :: hash: " + d.getHash());
                    System.out.println(" :: timestamp: " + Instant.ofEpochMilli(d.getTimestamp()).toString());
                }
            });

            // add message label if not empty
            if(!d.getMessage().isEmpty()) {
                JLabel msgLabel = new JLabel(d.getMessage());
                GridBagConstraints gcMsgLabel = new GridBagConstraints();
                gcMsgLabel.fill = GridBagConstraints.BOTH;
                gcMsgLabel.insets = new Insets(0, 0, 5, 5);
                gcMsgLabel.gridx = 0;
                gcMsgLabel.gridy = 0;
                docPanel.add(msgLabel, gcMsgLabel);
            }

            // add hash label for ID
            JLabel hashLabel = new JLabel(d.getHash());
            GridBagConstraints gcHashLabel = new GridBagConstraints();
            gcHashLabel.fill = GridBagConstraints.BOTH;
            gcHashLabel.insets = new Insets(0, 0, 5, 5);
            gcHashLabel.gridx = 0;
            gcHashLabel.gridy = 1;
            docPanel.add(hashLabel, gcHashLabel);

            // add time label
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(d.getTimestamp()), ZoneOffset.UTC);
            String timeLabelText = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " UTC";
            JLabel timeLabel = new JLabel(timeLabelText);
            GridBagConstraints gcTimeLabel = new GridBagConstraints();
            gcTimeLabel.fill = GridBagConstraints.BOTH;
            gcTimeLabel.insets = new Insets(0, 0, 5, 5);
            gcTimeLabel.gridx = 0;
            gcTimeLabel.gridy = 2;
            docPanel.add(timeLabel, gcTimeLabel);
            docListPanel.add(docPanel);
        });
    }

    /**
     * basic JTextField creator utility
     * @param label
     * @param position
     * @param containerPanel
     * @return JTextField
     */
    private JTextField buildTextField(String label, int position, JPanel containerPanel) {
        JLabel lbl = new JLabel(label);
        containerPanel.add(lbl, gbc(0, position, new int[] {0, 0, 5, 5}));

        JTextField txt = new JTextField();
        containerPanel.add(txt, gbc(1, position, new int[] {0, 0, 5, 5}));
        txt.setColumns(10);

        return txt;
    }

    public List<KeyDocument> getKeyDocuments() {
        return keyDocuments;
    }
}
