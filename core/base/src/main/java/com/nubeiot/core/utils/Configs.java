package com.nubeiot.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

import com.nubeiot.core.NubeLauncher;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Configs {

    private static final Logger logger = LoggerFactory.getLogger(Configs.class);
    private static final String SYSTEM_CFG_KEY = "__system__";
    private static final String DEPLOY_CFG_KEY = "__deploy__";
    private static final String APP_CFG_KEY = "__app__";
    public static final String EVENT_BUS_CFG_KEY = "__eventBus__";
    public static final String CLUSTER_CFG_KEY = "__cluster__";

    public static JsonObject loadDefaultConfig(String file) {
        return loadDefaultConfig(NubeLauncher.class, file);
    }

    public static JsonObject loadDefaultConfig(Class<?> clazz, String file) {
        final InputStream resourceAsStream = clazz.getClassLoader().getResourceAsStream(file);
        if (Objects.isNull(resourceAsStream)) {
            logger.warn("File not found");
            return new JsonObject();
        }
        try (Scanner scanner = new Scanner(resourceAsStream).useDelimiter("\\A")) {
            return new JsonObject(scanner.next());
        } catch (DecodeException e) {
            logger.warn("Config file is not valid JSON object", e);
            return new JsonObject();
        }
    }

    public static JsonObject getSystemCfg(JsonObject config) {
        return config.getJsonObject(SYSTEM_CFG_KEY, new JsonObject());
    }

    public static JsonObject getDeployCfg(JsonObject config) {
        return config.getJsonObject(DEPLOY_CFG_KEY, new JsonObject());
    }

    public static JsonObject getApplicationCfg(JsonObject config) {
        return config.getJsonObject(APP_CFG_KEY, new JsonObject());
    }

    public static JsonObject toApplicationCfg(JsonObject config) {
        return new JsonObject().put(APP_CFG_KEY, config);
    }

    public static Properties loadPropsConfig(String file) {
        Properties properties = new Properties();
        final InputStream resourceAsStream = Reflections.staticClassLoader().getResourceAsStream(file);
        if (Objects.isNull(resourceAsStream)) {
            logger.warn("File not found");
            return properties;
        }
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            logger.warn("Cannot load to properties file", e);
        } finally {
            FileUtils.silentClose(resourceAsStream);
        }
        return properties;
    }

}
