package com.nubeiot.core.common.utils;

import com.nubeiot.core.common.Launcher;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Runner {
    public static void runExample(String JAVA_DIR, Class clazz) {
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        if (deploymentOptions.getConfig() == null) {
            deploymentOptions.setConfig(new JsonObject());
        }

        File conf = new File(JAVA_DIR.split("/")[0], "src/conf/config.json");
        deploymentOptions.setConfig(getConfiguration(conf).mergeIn(deploymentOptions.getConfig()));
        runExample(JAVA_DIR, clazz, new VertxOptions().setClustered(true), deploymentOptions);
    }

    private static JsonObject getConfiguration(File config) {
        JsonObject conf = new JsonObject();
        conf = Launcher.getEntries(config, conf);
        return conf;
    }

    private static void runExample(String exampleDir, Class clazz, VertxOptions options, DeploymentOptions deploymentOptions) {
        runExample(exampleDir + clazz.getPackage().getName().replace(".", "/"), clazz.getName(), options, deploymentOptions);
    }

    private static void runExample(String exampleDir, String verticleID, VertxOptions options, DeploymentOptions deploymentOptions) {
        if (options == null) {
            // Default parameter
            options = new VertxOptions();
        }
        // Smart cwd detection

        // Based on the current directory (.) and the desired directory (exampleDir), we try to compute the vertx.cwd
        // directory:
        try {
            // We need to use the canonical file. Without the file name is .
            File current = new File(".").getCanonicalFile();
            if (exampleDir.startsWith(current.getName()) && !exampleDir.equals(current.getName())) {
                exampleDir = exampleDir.substring(current.getName().length() + 1);
            }
        } catch (IOException e) {
            // Ignore it.
            e.printStackTrace();
        }

        System.setProperty("vertx.cwd", exampleDir);
        Consumer<Vertx> runner = vertx -> {
            try {
                if (deploymentOptions != null) {
                    vertx.deployVerticle(verticleID, deploymentOptions);
                } else {
                    vertx.deployVerticle(verticleID);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
        if (options.isClustered()) {
            Vertx.clusteredVertx(options, res -> {
                if (res.succeeded()) {
                    Vertx vertx = res.result();
                    runner.accept(vertx);
                } else {
                    res.cause().printStackTrace();
                }
            });
        } else {
            Vertx vertx = Vertx.vertx(options);
            runner.accept(vertx);
        }
    }
}
