package org.white5moke.handoff.client;

import javax.swing.*;
import java.awt.*;

public class HandoffWindow extends JFrame {
    public HandoffWindow(String title, int w, int h) {
        super(title);
        setSize(new Dimension(w, h));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);

    }

    public static void main(String[] args) {
        new HandoffWindow("handoff", 480, 540);
    }
}
