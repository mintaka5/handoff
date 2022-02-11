package org.white5moke.handoff;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.concurrent.atomic.AtomicInteger;

public class HandoffWindow extends JFrame {
    private final Container basePanel;
    private Handoff handoff;
    private JButton newKeyButton;
    private JTextField messageText;

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
                handoff.create(messageText.getText());
                messageText.setText("");
            }
        });
    }

    private void buildUI() {
        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        JPanel messagePanel = new JPanel();
        messagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagLayout messageLayout = new GridBagLayout();
        messageLayout.columnWidths = new int[] {86, 86, 0};
        messageLayout.rowHeights = new int[] {20, 20, 20, 20, 20, 0};
        messageLayout.columnWeights = new double[] {0, 1, Double.MIN_VALUE};
        messageLayout.rowWeights = new double[] {0, 0, 0, 0, 0, Double.MIN_VALUE};
        messagePanel.setLayout(messageLayout);

        messageText = buildTextField("message", 0, messagePanel);
        center.add(BorderLayout.CENTER, messagePanel);

        getContentPane().add(BorderLayout.CENTER, center);

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
}
