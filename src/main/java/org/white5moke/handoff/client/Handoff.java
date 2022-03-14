package org.white5moke.handoff.client;

import org.apache.commons.lang3.StringUtils;
import org.white5moke.handoff.doc.TheStore;

import java.nio.file.Path;
import java.security.Security;
import java.util.Arrays;
import java.util.Scanner;

public class Handoff implements Runnable {
    private Path userHome;
    private HandoffCommands commands;
    private Scanner scan;
    private TheStore store;

    public Handoff() {
        Security.setProperty("crypto.policy", "unlimited");

        setUserHome(Path.of(System.getProperty("user.home"), ".handoff"));

        setScan(new Scanner(System.in));

        setStore(new TheStore(getUserHome()));

        setCommands(new HandoffCommands(getStore(), getScan()));
    }

    @Override
    public void run() {
        while(true) {
            System.out.print(">> ");

            String userInput = scan.nextLine().strip();

            String[] parseS = StringUtils.split(userInput, StringUtils.SPACE);

            // skip over empties
            if(parseS.length <= 0) continue;

            // lop off command text, and use the rest of the string
            String[] theRest = Arrays.copyOfRange(parseS, 1, parseS.length);
            String theMessage = StringUtils.join(theRest, StringUtils.SPACE).strip();

            switch (parseS[0].strip()) {
                case "gen", "generate" -> commands.generateDocument(theMessage);
                case "bye", "exit", "quit" -> commands.sayGoodbye();
                case "hash" -> commands.hashIt(theMessage);
                case "list", "ls", "keys" -> commands.listEm(getStore());
                case "use", "select", "pick" -> commands.selectIt(theMessage);
                default -> {}
            }
        }
    }

    public Path getUserHome() {
        return userHome;
    }

    public void setUserHome(Path userHome) {
        this.userHome = userHome;
    }

    public HandoffCommands getCommands() {
        return commands;
    }

    public void setCommands(HandoffCommands commands) {
        this.commands = commands;
    }

    public Scanner getScan() {
        return scan;
    }

    public void setScan(Scanner scan) {
        this.scan = scan;
    }

    public TheStore getStore() {
        return store;
    }

    public void setStore(TheStore store) {
        this.store = store;
    }
}
