package org.white5moke.handoff;

import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Botnik implements Runnable {
    private final Scanner scanner = new Scanner(System.in);
    private static ExecutorService exec = Executors.newSingleThreadExecutor();

    public Botnik() {

    }

    @Override
    public void run() {
        while(scanner.hasNext()) {
            Instant now = Instant.now();
            String tag = Utilities.randomID(4);

            if(scanner.next().equals("exit")) break;

            // operate
            System.out.print(">> ");
            String input = scanner.next().strip();

            respond(input);
        }
        System.out.println("bye x.x");
        System.exit(0);
    }

    private void respond(String input) {
        Training training = new Training(input);
    }

    public static void main(String[] args) {
        exec = Executors.newSingleThreadExecutor();
        exec.execute(new Botnik());
    }
}

class Training implements Runnable {
    private String input;

    public Training(String input) {
        setInput(input);
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public void run() {

    }
}
