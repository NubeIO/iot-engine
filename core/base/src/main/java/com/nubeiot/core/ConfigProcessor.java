package com.nubeiot.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.NubeConfig.DeployConfig;
import com.nubeiot.core.NubeConfig.SystemConfig;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;

public class ConfigProcessor {

    private static final String NUBEIO = "NUBEIO";
    private static final String APP = "app";
    private static final String SYSTEM = "system";
    private static final String DEPLOY = "deploy";
    private static final String DATA_DIR = "dataDir";
    private static final String DATA_DIR_LOWER_CASE = "datadir";
    @Getter
    private LinkedHashMap<ConfigStoreOptions, Function<JsonObject, Map<String, Object>>> mappingOptions;
    private Vertx vertx;

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
            .filter(x -> x.getKey().startsWith(NUBEIO))
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
                .filter(x -> x.getKey().startsWith(NUBEIO.toLowerCase()))
                .collect(Collectors.toMap(key -> convertEnv(key.getKey()),
                                          Entry::getValue));
            return new JsonObject(collect);
        });
        retriever.getConfig(json -> {});
        return retriever.getCachedConfig();
    }

    private static String convertEnv(String key) {
        if (Strings.isBlank(key)) {
            return key;
        }
        return lowerCase(key).replace('_', '.');
    }

    private static String lowerCase(String key) {
        if (Objects.isNull(key)) {
            return "";
        } else {
            return key.toLowerCase(Locale.ENGLISH);
        }
    }


    public Map<String, Object> mergeEnvAndSys() {
        Map<String, Object> result = new HashMap<>();
        mappingOptions.forEach((store, filterNubeVariables) -> {
            ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(store);
            ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
            retriever.getConfig(json -> {
                Map<String, Object> currentResult = filterNubeVariables.apply(json.result());
                result.putAll(currentResult);
            });
        });
        return new TreeMap<>(result);
    }

    public <T extends IConfig> Optional<T> processAndOverride(@NonNull Class<T> clazz, IConfig provideConfig,
                                                              IConfig defaultConfig) {
        Map<String, Object> envConfig = this.mergeEnvAndSys();
        IConfig input;
        if (Objects.isNull(provideConfig)) {
            input = defaultConfig;
        } else if (Objects.isNull(defaultConfig)) {
            input = provideConfig;
        } else {
            input = IConfig.merge(defaultConfig, provideConfig, clazz);
        }
        return overrideConfig(clazz, envConfig, input);
    }

    private <T extends IConfig> Optional<T> overrideConfig(Class<T> clazz, Map<String, Object> envConfig,
                                                           IConfig input) {
        JsonObject nubeConfig = new JsonObject();

        JsonObject inputAppConfig = input.toJson().getJsonObject(AppConfig.NAME);
        JsonObject inputSystemConfig = input.toJson().getJsonObject(SystemConfig.NAME);
        JsonObject inputDeployConfig = input.toJson().getJsonObject(DeployConfig.NAME);
        JsonObject appConfigJson = new JsonObject();
        JsonObject systemConfig = new JsonObject();
        JsonObject deployConfig = new JsonObject();

        for (Map.Entry<String, Object> entry : envConfig.entrySet()) {
            String[] keyArray = entry.getKey().split("\\.");

            switch (keyArray[1]) {
                case APP:
                    handleConfig(appConfigJson, inputAppConfig, entry, keyArray);
                    break;
                case SYSTEM:
                    handleConfig(systemConfig, inputSystemConfig, entry, keyArray);
                    break;
                case DEPLOY:
                    handleConfig(deployConfig, inputDeployConfig, entry, keyArray);
                    break;
                case DATA_DIR_LOWER_CASE:
                    try {
                        if (!String.class.isInstance(entry.getValue())) {
                            continue;
                        }
                        FileUtils.toPath(entry.getValue().toString());
                    } catch (NubeException ex) {
                        continue;
                    }
                    nubeConfig.put(DATA_DIR, entry.getValue());
                    break;
            }
        }

        nubeConfig.put(AppConfig.NAME, new JsonObject(inputAppConfig.toString()).mergeIn(appConfigJson, true));
        nubeConfig.put(SystemConfig.NAME, new JsonObject(inputSystemConfig.toString()).mergeIn(systemConfig, true));
        nubeConfig.put(DeployConfig.NAME, new JsonObject(inputDeployConfig.toString()).mergeIn(deployConfig, true));

        try {
            return Optional.of(IConfig.MAPPER.copy()
                                   .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                   .readValue(nubeConfig.toString(), clazz));
        } catch (IOException e) {
            throw new ClassCastException(e.getLocalizedMessage());
        }
    }

    private void handleConfig(JsonObject config, JsonObject inputConfig, Entry<String, Object> entry,
                              String[] keyArray) {
        if (Objects.isNull(inputConfig)) {
            return;
        }
        if (keyArray.length < 3) {
            return;
        }
        String propertyName = keyArray[2];
        if (Objects.nonNull(inputConfig.getValue(propertyName))) {
            this.handleChildElement(config, inputConfig, entry, keyArray, propertyName);
        } else if (Objects.nonNull(inputConfig.getValue("__" + propertyName + "__"))) {
            this.handleChildElement(config, inputConfig, entry, keyArray, "__" + propertyName + "__");
        } else {
            this.handleChildElement(config, inputConfig, entry, keyArray, propertyName);
        }
    }

    private void handleChildElement(JsonObject appConfig, JsonObject
        inputAppConfig, Entry<String, Object> entry,
                                    String[] keyArray, String temp) {
        if (keyArray.length < 4) {
            appConfig.put(keyArray[2], entry.getValue());
            return;
        }
        JsonObject overrideResult = checkAndUpdate(3, keyArray,
                                                   entry.getValue(),
                                                   inputAppConfig);
        if (Objects.isNull(overrideResult)) {
            return;
        }
        JsonObject childElement = appConfig.getJsonObject(temp);
        if (Objects.isNull(childElement)) {
            appConfig.put(temp, overrideResult);
        } else {
            childElement = childElement.mergeIn(overrideResult);
        }
    }


    private void initDefaultOptions() {
        initSystemOption();
        initEnvironmentOption();
    }

    private void initSystemOption() {
        ConfigStoreOptions systemStore = new ConfigStoreOptions().setType("sys").setOptional(true);
        mappingOptions.put(systemStore, entries -> entries.stream()
            .filter(x -> x.getKey().startsWith(NUBEIO.toLowerCase()))
            .collect(
                Collectors.toMap(key -> convertEnv(key.getKey()),
                                 Entry::getValue)));
    }

    private void initEnvironmentOption() {
        ConfigStoreOptions environmentStore = new ConfigStoreOptions().setType("env").setOptional(true);
        mappingOptions.put(environmentStore, entries -> entries.stream()
            .filter(x -> x.getKey().startsWith(NUBEIO))
            .collect(
                Collectors.toMap(key -> convertEnv(key.getKey()),
                                 Entry::getValue)));
    }

    private JsonObject checkAndUpdate(int index, String[] array, Object overrideValue, JsonObject input)
        throws ClassCastException {
        String key = array[index];
        Object value = input.getValue(key);
        if (Objects.nonNull(value)) {
            if (JsonObject.class.isInstance(value)) {
                JsonObject temp = checkAndUpdate(index + 1, array, overrideValue, (JsonObject) value);
                if (Objects.nonNull(temp)) {
                    return new JsonObject().put(key, temp);
                }
                return null;
            }
            if (index == array.length - 1) {
                return new JsonObject().put(key, overrideValue);
            }
            return null;
        } else if (Objects.nonNull(input.getValue("__" + key + "__"))) {
            value = input.getValue("__" + key + "__");
            if (JsonObject.class.isInstance(value)) {
                JsonObject temp = checkAndUpdate(index + 1, array, overrideValue, (JsonObject) value);
                if (Objects.nonNull(temp)) {
                    return new JsonObject().put("__" + key + "__",
                                                temp);
                }
                ;
                return null;
            }
            return null;
        } else {
            //if input Value doesn't contains this attribute.
            if (index == array.length - 1) {
                return new JsonObject().put(key, overrideValue);
            }
            JsonObject temp = checkAndUpdate(index + 1, array, overrideValue, new JsonObject());
            if (Objects.nonNull(temp)) {
                return new JsonObject().put(key, temp);
            }
            return null;
        }
    }

}
