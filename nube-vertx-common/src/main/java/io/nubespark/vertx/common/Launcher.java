package io.nubespark.vertx.common;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by topsykretts on 4/26/18.
 */
public class Launcher extends io.vertx.core.Launcher {

    public static void main(String[] args) {
        new Launcher().dispatch(args);
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        if (!options.isClustered()) {
            options.setClustered(true);
        }
        System.out.println("Enabling vertx event bus encryption...");
        //set encryption in vertx event bus
        //todo change keystore in production
        options.setEventBusOptions(
            new EventBusOptions()
                .setSsl(true)
                .setKeyStoreOptions(new JksOptions().setPath("eventBusKeystore.jks").setPassword("nubesparkEventBus"))
                .setTrustStoreOptions(new JksOptions().setPath("eventBusKeystore.jks").setPassword("nubesparkEventBus"))
                .setClientAuth(ClientAuth.REQUIRED)
        );
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        super.beforeDeployingVerticle(deploymentOptions);
        if (deploymentOptions.getConfig() == null) {
            deploymentOptions.setConfig(new JsonObject());
        }

        File conf = new File("src/conf/config.json");
        deploymentOptions.setConfig(getConfiguration(conf).mergeIn(deploymentOptions.getConfig()));
    }

    private JsonObject getConfiguration(File config) {
        JsonObject conf = new JsonObject();
        if (config.isFile()) {
            System.out.println("Reading config file: " + config.getAbsolutePath());
            try (Scanner scanner = new Scanner(config).useDelimiter("\\A")) {
                String sconf = scanner.next();
                try {
                    conf = new JsonObject(sconf);
                } catch (DecodeException e) {
                    System.err.println("Configuration file " + sconf + " does not contain a valid JSON object");
                }
            } catch (FileNotFoundException e) {
                // Ignore it.
                e.printStackTrace();
            }
        } else {
            System.out.println("Config file not found " + config.getAbsolutePath());
        }
        return conf;
    }
}
