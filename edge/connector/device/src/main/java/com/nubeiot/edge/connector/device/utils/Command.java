package com.nubeiot.edge.connector.device.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class Command {

    private final static Logger logger = LoggerFactory.getLogger(Command.class);

    public static String execute(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        StringBuilder output = new StringBuilder();
        // Run a shell command
        processBuilder.command("bash", "-c", command);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                return output.toString().trim();
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Could not run command line: " + command, e);
        }
        return null;
    }

    public static List<String> executeWithSplit(String command) {
        String executeResult = Command.execute(command);
        if (executeResult != null) {
            return Arrays.asList(executeResult.split("\n"));
        }
        return new ArrayList<>();
    }

}
