package com.nubeiot.edge.connector.device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public interface UnixCommand {

    Logger logger = LoggerFactory.getLogger(UnixCommand.class);

    default String execute(String command) {
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

}
