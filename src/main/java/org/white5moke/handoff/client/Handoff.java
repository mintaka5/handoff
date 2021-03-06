package org.white5moke.handoff.client;

import org.apache.commons.lang3.StringUtils;
import org.white5moke.handoff.doc.TheStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Scanner;

public class Handoff implements Runnable {
    private Path userHome;
    private HandoffCommands commands;
    private Scanner scan;
    private TheStore store;

    private boolean isRunning = false;

    public Handoff() {
        Security.setProperty("crypto.policy", "unlimited");

        setUserHome(Path.of(System.getProperty("user.home"), ".handoff"));

        setScan(new Scanner(System.in));

        setStore(new TheStore(getUserHome()));

        setCommands(new HandoffCommands(getStore(), getScan()));
    }

    @Override
    public void run() {
        // set a current doc hash if it's not set
        doCurrentCheck();

        setRunning(true);

        while(isRunning()) {
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
                case "bye", "exit", "quit" -> {
                    setRunning(false);
                    System.out.println("<< exiting...");
                    System.exit(0);
                }
                case "hash" -> commands.hashIt(theMessage);
                case "list", "ls", "keys" -> commands.listEm(getStore());
                case "use", "select", "pick" -> commands.useIt(theMessage);
                case "current", "cur" -> commands.currentDoc();
                case "help" -> commands.helpMe(theMessage);
                case "peek", "show", "deets", "view" -> commands.deets(theMessage);
                case "sign" -> commands.signIt(theMessage);
                case "verify" -> commands.verifyIt(theMessage);
                default -> commands.four0Four();
            }
        }
    }

    /**
     * sloppy i know but i didn't want to dirty up run()
     */
    private void doCurrentCheck() {
        Path file = null;
        try {
            file = Files.list(getUserHome()).min((a, b) -> Long.compare(b.toFile().lastModified(), a.toFile().lastModified())).orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if not null, and is empty, set a current hash
        if (getStore().getCurrentHash().isEmpty() && file != null)
            getStore().setCurrentHash(file.getFileName().toString());
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");

        if(file != null) {
            System.out.printf("<< current key doc: `%s` @ %s%n",
                    getStore().getCurrentHash(),
                    formatter.format(Date.from(Instant.ofEpochMilli(file.toFile().lastModified()))));
        } else {
            System.out.println("<x no key documents found. generate one!");
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

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}
