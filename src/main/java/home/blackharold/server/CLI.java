package home.blackharold.server;

import home.blackharold.server.impl.ServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class CLI {

    private static final Logger LOG = LoggerFactory.getLogger(CLI.class);
    private static final List<String> QUIT_CMDS = Collections.unmodifiableList(Arrays.asList(new String[]{"q", "quit", "exit"}));

    public static void main(String[] args) {
        Thread.currentThread().setName("CLI-Main");

        try {
            Server server = ServerFactory.buildServer(null);
            server.start();
            waitForStopCommand(server);
        } catch (Exception e) {
            LOG.error("Can't execute cmd: " + e.getMessage(), e);
        }
    }

    private static void waitForStopCommand(Server server) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            while (true) {
                String cmd = scanner.nextLine();
                if (QUIT_CMDS.contains(cmd.toLowerCase())) {
                    server.stop();
                    break;
                } else {
                    LOG.error("Undefined command: " + cmd + "! To shutdown server type 'q'");
                }
            }
        }
    }
}
