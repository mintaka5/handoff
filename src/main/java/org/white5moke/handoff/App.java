package org.white5moke.handoff;

import org.white5moke.handoff.client.Handoff;

public class App {
    public App() {
        Thread t1 = new Thread(new Handoff());
        t1.start();
    }

    public static void main(String[] args) {
        new App();
    }
}