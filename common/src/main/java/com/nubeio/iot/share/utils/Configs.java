package com.nubeio.iot.share.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

import com.hazelcast.config.ConfigBuilder;
import com.hazelcast.config.XmlConfigBuilder;
import com.nubeio.iot.share.NubeLauncher;
import com.nubeio.iot.share.exceptions.NubeException;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Configs {

    private static final Logger logger = LoggerFactory.getLogger(Configs.class);

    public static JsonObject loadDefaultConfig(String file) {
        final InputStream resourceAsStream = NubeLauncher.class.getClassLoader().getResourceAsStream(file);
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

    public static Properties loadPropsConfig(String file) {
        Properties properties = new Properties();
        final InputStream resourceAsStream = NubeLauncher.class.getClassLoader().getResourceAsStream(file);
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

    public static ConfigBuilder parseClusterConfig(JsonObject clusterOption) {
        URL url = FileUtils.toUrl(clusterOption.getString("url", null));
        try {
            if (Objects.nonNull(url)) {
                return new XmlConfigBuilder(url);
            } else {
                Path path = FileUtils.toPath(clusterOption.getString("file"), "cluster.xml");
                return new XmlConfigBuilder(path.toAbsolutePath().toString());
            }
        } catch (IOException | NubeException | IllegalArgumentException ex) {
            logger.info("Fallback to default", ex);
            return new XmlConfigBuilder();
        }
    }

}
