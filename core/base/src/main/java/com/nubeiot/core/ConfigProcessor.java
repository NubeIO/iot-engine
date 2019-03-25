package com.nubeiot.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.NubeConfig.AppConfig;

import lombok.Getter;
import lombok.NonNull;

public class ConfigProcessor {

    private static final String nubeio = "NUBEIO";
    @Getter
    private LinkedHashMap<ConfigStoreOptions, Function<JsonObject, Map<String, Object>>> mappingOptions;
    private Vertx vertx;
    @Getter
    private Map<String, Object> result = new HashMap<>();

    public ConfigProcessor(Vertx vertx) {
        this.vertx = vertx;
        mappingOptions = new LinkedHashMap<>();
        initDefaultOptions();
    }

    //To use in test. will be removed later
    public static Map<String, Object> getConfigFromEnvironment(Vertx vertx) {
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
        return mappedEnv;
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

    public void process() {
        mappingOptions.forEach((store, translate) -> {
            ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(store);
            ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
            retriever.getConfig(json -> {
            });

            Map<String, Object> currentResult = translate.apply(retriever.getCachedConfig());
            currentResult.putAll(result);
            result = currentResult;
        });
    }

    public <T extends IConfig> Optional<T> processAndOverride(@NonNull Class<T> clazz, AppConfig input) {
        process();
        return overrideConfig(clazz, input);
    }

    private <T extends IConfig> Optional<T> overrideConfig(Class<T> clazz, AppConfig input) {
        T finalAppConfig;
        try {
            finalAppConfig = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return Optional.empty();
        }
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Optional<JsonObject> config = overrideConfig(key, value.toString(), input);
            if (config.isPresent()) {
                T appConfig = IConfig.from(config.get(), clazz);
                finalAppConfig = IConfig.merge(finalAppConfig, appConfig, clazz);
            }
        }
        return Optional.of(finalAppConfig);
    }

    private Optional<JsonObject> overrideConfig(String key, String value, AppConfig input) {
        String[] array = key.split("\\.");
        if (!array[0].equals(nubeio.toLowerCase())) {
            return Optional.empty();
        }
        if (!hasMatchChildConfig(array, input)) {
            return Optional.empty();
        }
        JsonObject finalResult = new JsonObject();
        for (int i = array.length - 1; i > 1; i--) {
            String item = array[i];
            if (i == array.length - 1) {
                finalResult.put(item, value);
            } else {
                if (input.get(item) != null) {
                    finalResult = get(item, finalResult);
                } else if (input.get("__" + item + "__") != null) {
                    finalResult = get("__" + item + "__", finalResult);
                } else {
                    return Optional.empty();
                }
            }
        }
        finalResult = new JsonObject().put(AppConfig.NAME, finalResult);
        return Optional.of(finalResult);
    }

    private boolean hasMatchChildConfig(String[] array, AppConfig input) {
        for (String item : Arrays.stream(array).skip(2).collect(Collectors.toList())) {
            if (input.get(item) != null || input.get("__" + item + "__") != null) {
                return true;
            }
        }
        return false;
    }

    private JsonObject get(String item, JsonObject jsonObject) {
        return new JsonObject().put(item, jsonObject);
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
