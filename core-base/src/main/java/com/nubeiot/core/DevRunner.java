package com.nubeiot.core;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import com.nubeiot.core.utils.Configs;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public final class DevRunner {

    public static void run(String javaDir, Class clazz) {
        final JsonObject launcherCfg = NubeLauncher.defaultConfig();
        final JsonObject appCfg = Configs.loadDefaultConfig(clazz, "config.json");
        JsonObject config = launcherCfg.mergeIn(appCfg);
        run(javaDir, clazz, NubeLauncher.defaultVertxOption(Configs.getSystemCfg(config)),
            new DeploymentOptions(Configs.getDeployCfg(config)).setConfig(Configs.getApplicationCfg(config)));
    }

    private static void run(String javaDir, Class clazz, VertxOptions options, DeploymentOptions deploymentOptions) {
        run(javaDir + clazz.getPackage().getName().replace(".", "/"), clazz.getName(), options, deploymentOptions);
    }

    private static void run(String workDir, String verticleID, VertxOptions options,
                            DeploymentOptions deploymentOptions) {
        System.setProperty("vertx.cwd", computeWorkDir(workDir));
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
            runner.accept(Vertx.vertx(options));
        }
    }

    private static String computeWorkDir(String workDir) {
        // Smart cwd detection
        // Based on the current directory (.) and the desired directory (exampleDir), we try to compute the vertx.cwd
        // directory:
        try {
            // We need to use the canonical file. Without the file name is .
            File current = new File(".").getCanonicalFile();
            if (workDir.startsWith(current.getName()) && !workDir.equals(current.getName())) {
                workDir = workDir.substring(current.getName().length() + 1);
            }
        } catch (IOException e) {
            // Ignore it.
            e.printStackTrace();
        }
        return workDir;
    }

}
