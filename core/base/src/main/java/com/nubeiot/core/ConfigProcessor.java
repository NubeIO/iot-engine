package com.nubeiot.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class ConfigProcessor {

    private static final String nubeio = "NUBEIO";
    @Getter
    private LinkedHashMap<ConfigStoreOptions, Function<JsonObject, Map<String, Object>>> mappingOptions;
    private Vertx vertx;
    @Getter
    private IConfig currentConfig = new NubeConfig();

    public ConfigProcessor(Vertx vertx) {
        this.vertx = vertx;
        mappingOptions = new LinkedHashMap<>();
        initDefaultOptions();
    }

    //To use in test. will be removed later
    public static JsonObject getConfigFromEnvironment(Vertx vertx) {
        ConfigStoreOptions environmentStore = new ConfigStoreOptions().setType("env");
        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(environmentStore);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
        retriever.getConfig(json -> {
        });
        Map<String, Object> mappedEnv = retriever.getCachedConfig()
                                                 .stream()
                                                 .filter(x -> x.getKey().startsWith(nubeio))
                                                 .collect(Collectors.toMap(key -> convertEnv(key.getKey()),
                                                                           Map.Entry::getValue));
        return new JsonObject(mappedEnv);
    }

    //To use in test. will be removed later
    public static JsonObject getConfigFromSystem(Vertx vertx) {
        ConfigStoreOptions systemStore = new ConfigStoreOptions().setType("sys").setOptional(true);
        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(systemStore);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
        retriever.setConfigurationProcessor(entries -> {
            final Map<String, Object> collect = entries.stream()
                                                       .filter(x -> x.getKey().startsWith(nubeio.toLowerCase()))
                                                       .collect(Collectors.toMap(key -> convertEnv(key.getKey()),
                                                                                 Entry::getValue));
            return new JsonObject(collect);
        });
        retriever.getConfig(json -> {});
        return retriever.getCachedConfig();
    }

    private static String convertEnv(String key) {
        if (isBlank(key)) {
            return key;
        }
        return lowerCase(key).replace('_', '.');
    }

    private static String lowerCase(String key) {
        if (key == null) {
            return null;
        } else {
            return key.toLowerCase();
        }
    }

    private static boolean isBlank(String key) {
        return (key == null || key.trim().equals(""));
    }

    public <T extends IConfig> void process(@NonNull Class<T> clazz) {
        mappingOptions.forEach((store, translate) -> {
            ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(store);
            ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
            retriever.getConfig(json -> {
            });
            currentConfig = IConfig.merge(new JsonObject(translate.apply(retriever.getCachedConfig())), currentConfig,
                                          clazz);
        });
    }

    private void initDefaultOptions() {
        initEnvironmentOption();
        initSystemOption();
    }

    private void initSystemOption() {
        ConfigStoreOptions systemStore = new ConfigStoreOptions().setType("sys").setOptional(true);
        mappingOptions.put(systemStore, entries -> entries.stream()
                                                               .filter(x -> x.getKey().startsWith(nubeio.toLowerCase()))
                                                               .collect(
                                                                   Collectors.toMap(key -> convertEnv(key.getKey()),
                                                                                    Entry::getValue)));
    }

    private void initEnvironmentOption() {
        ConfigStoreOptions environmentStore = new ConfigStoreOptions().setType("env").setOptional(true);
        mappingOptions.put(environmentStore, entries -> entries.stream()
                                                               .filter(x -> x.getKey().startsWith(nubeio))
                                                               .collect(
                                                                   Collectors.toMap(key -> convertEnv(key.getKey()),
                                                                                    Entry::getValue)));
    }

}
