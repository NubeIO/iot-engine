package com.nubeiot.core;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.NubeConfig.DeployConfig;
import com.nubeiot.core.NubeConfig.SystemConfig;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public class ConfigProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ConfigProcessor.class);

    private static final String NUBEIO_SYS = "NUBEIO.";
    private static final String NUBEIO_ENV = "NUBEIO_";
    private static final String APP = "app";
    private static final String SYSTEM = "system";
    private static final String DEPLOY = "deploy";
    private static final String DATA_DIR = "dataDir";
    private static final String DATA_DIR_LOWER_CASE = "datadir";
    private LinkedHashMap<ConfigStoreOptions, Function<JsonObject, Map<String, Object>>> mappingOptions;
    private Vertx vertx;

    public ConfigProcessor(Vertx vertx) {
        this.vertx = vertx;
        mappingOptions = new LinkedHashMap<>();
        initDefaultOptions();
    }

    private String convertEnv(String key) {
        if (Strings.isBlank(key)) {
            return key;
        }
        return key.toLowerCase(Locale.ENGLISH).replace('_', '.');
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
        if (Objects.isNull(provideConfig) && Objects.isNull(defaultConfig)) {
            return Optional.empty();
        }
        Map<String, Object> envConfig = this.mergeEnvAndSys();
        IConfig input;
        if (Objects.isNull(provideConfig)) {
            logger.debug("Provide config is null");
            input = defaultConfig;
        } else if (Objects.isNull(defaultConfig)) {
            logger.debug("Default config is null");
            input = provideConfig;
        } else {
            input = IConfig.merge(defaultConfig, provideConfig, clazz);
        }
        return overrideConfig(clazz, envConfig, input.toJson());
    }

    private <T extends IConfig> Optional<T> overrideConfig(Class<T> clazz, Map<String, Object> envConfig,
                                                           JsonObject input) {
        JsonObject nubeConfig = new JsonObject();

        JsonObject inputAppConfig = input.getJsonObject(AppConfig.NAME);
        JsonObject inputSystemConfig = input.getJsonObject(SystemConfig.NAME);
        JsonObject inputDeployConfig = input.getJsonObject(DeployConfig.NAME);
        JsonObject appConfig = new JsonObject();
        JsonObject systemConfig = new JsonObject();
        JsonObject deployConfig = new JsonObject();

        for (Map.Entry<String, Object> entry : envConfig.entrySet()) {
            String[] keyArray = entry.getKey().split("\\.");
            Object overrideValue = entry.getValue();
            //TODO should be able to handle the generic case
            switch (keyArray[1]) {
                case APP:
                    handleConfig(appConfig, inputAppConfig, overrideValue, keyArray);
                    break;
                case SYSTEM:
                    handleConfig(systemConfig, inputSystemConfig, overrideValue, keyArray);
                    break;
                case DEPLOY:
                    handleConfig(deployConfig, inputDeployConfig, overrideValue, keyArray);
                    break;
                case DATA_DIR_LOWER_CASE:
                    try {
                        if (!String.class.isInstance(overrideValue)) {
                            continue;
                        }
                        FileUtils.toPath(overrideValue.toString());
                    } catch (NubeException ex) {
                        logger.warn("DataDir is not valid. ", ex);
                        continue;
                    }
                    nubeConfig.put(DATA_DIR, overrideValue);
                    break;
            }
        }

        nubeConfig.put(AppConfig.NAME, new JsonObject(inputAppConfig.toString()).mergeIn(appConfig, true));
        nubeConfig.put(SystemConfig.NAME, new JsonObject(inputSystemConfig.toString()).mergeIn(systemConfig, true));
        nubeConfig.put(DeployConfig.NAME, new JsonObject(inputDeployConfig.toString()).mergeIn(deployConfig, true));

        try {
            return Optional.of(IConfig.MAPPER.copy()
                                   .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                   .readValue(nubeConfig.toString(), clazz));
        } catch (IOException ex) {
            logger.warn("Converting to object failed", ex);
            throw new NubeException(ex);
        }
    }

    private void handleConfig(JsonObject config, JsonObject inputConfig, Object overrideValue,
                              String[] keyArray) {
        if (Objects.isNull(inputConfig)) {
            return;
        }
        if (keyArray.length < 3) {
            return;
        }
        String propertyName = keyArray[2];
        Optional<Entry<String, Object>> valueByKey = this.getValueByKey(inputConfig, propertyName);
        if (valueByKey.isPresent() && Objects.nonNull(valueByKey.get().getValue())) {
            this.handleChildElement(config, overrideValue, keyArray,
                                    valueByKey.get());
            return;
        }

        valueByKey = this.getValueByKey(inputConfig, "__" + propertyName + "__");
        if (valueByKey.isPresent() && Objects.nonNull(valueByKey.get().getValue())) {
            this.handleChildElement(config, overrideValue, keyArray,
                                    valueByKey.get());
        } else {
            this.handleChildElement(config, overrideValue, keyArray, new SimpleEntry<String, Object>(propertyName,
                                                                                                     new JsonObject()));
        }
    }

    private void handleChildElement(JsonObject appConfig, Object overrideValue,
                                    String[] keyArray, Entry<String, Object> valueByKey) {
        if (keyArray.length < 4 || !JsonObject.class.isInstance(valueByKey.getValue())) {
            appConfig.put(valueByKey.getKey(), overrideValue);
            return;
        }
        JsonObject overrideResult = checkAndUpdate(3, keyArray,
                                                   overrideValue,
                                                   (JsonObject) valueByKey.getValue());
        if (Objects.isNull(overrideResult)) {
            return;
        }
        JsonObject childElement = appConfig.getJsonObject(valueByKey.getKey());
        if (Objects.isNull(childElement)) {
            appConfig.put(valueByKey.getKey(), overrideResult);
        } else {
            overrideResult.getMap().forEach((key, val) -> {
                childElement.put(key, val);
            });
        }
    }

    private void initDefaultOptions() {
        initSystemOption();
        initEnvironmentOption();
    }

    private void initSystemOption() {
        ConfigStoreOptions systemStore = new ConfigStoreOptions().setType("sys").setOptional(true);
        mappingOptions.put(systemStore, entries -> entries.stream()
            .filter(x -> x.getKey().startsWith(NUBEIO_SYS.toLowerCase()))
            .collect(
                Collectors.toMap(entry -> convertEnv(entry.getKey()),
                                 Entry::getValue)));
    }

    private void initEnvironmentOption() {
        ConfigStoreOptions environmentStore = new ConfigStoreOptions().setType("env").setOptional(true);
        mappingOptions.put(environmentStore, entries -> entries.stream()
            .filter(x -> x.getKey().startsWith(NUBEIO_ENV))
            .collect(
                Collectors.toMap(entry -> convertEnv(entry.getKey()),
                                 Entry::getValue)));
    }

    private JsonObject checkAndUpdate(int index, String[] array, Object overrideValue, JsonObject input) {
        String key = array[index];
        Optional<Entry<String, Object>> valueByKey = this.getValueByKey(input, key);
        if (valueByKey.isPresent() && Objects.nonNull(valueByKey.get().getValue())) {
            return checkingNextLevel(index, array, overrideValue, valueByKey.get());
        }
        valueByKey = this.getValueByKey(input, "__" + key + "__");

        if (valueByKey.isPresent() && Objects.nonNull(valueByKey.get().getValue())) {
            return checkingNextLevel(index, array, overrideValue, valueByKey.get());
        }
        return checkingNextLevel(index, array, overrideValue, new SimpleEntry<String, Object>(key, null));
    }

    private JsonObject checkingNextLevel(int index, String[] array, Object overrideValue,
                                         Entry<String, Object> entry) {
        if (index == array.length - 1) {
            if (Objects.isNull(entry.getValue())) {
                return new JsonObject().put(entry.getKey(), overrideValue);
            }
            try {
                Object temp = entry.getValue().getClass().cast(overrideValue);
                return new JsonObject().put(entry.getKey(), temp);
            } catch (ClassCastException ex) {
                logger.warn("Invalid data type", ex);
                if (Integer.class.isInstance(entry.getValue()) && Number.class.isInstance(overrideValue)) {
                    logger.warn("Source data type is Integer but input is Number");
                    return new JsonObject().put(entry.getKey(), ((Number) overrideValue).intValue());
                }
                return null;
            }
        }
        JsonObject temp;
        if (Objects.isNull(entry.getValue())) {
            temp = checkAndUpdate(index + 1, array, overrideValue, new JsonObject());
        } else if (JsonObject.class.isInstance(entry.getValue())) {
            temp = checkAndUpdate(index + 1, array, overrideValue, (JsonObject) entry.getValue());
        } else {
            return null;
        }
        if (Objects.nonNull(temp)) {
            return new JsonObject().put(entry.getKey(), temp);
        }
        return null;
    }

    private Optional<Entry<String, Object>> getValueByKey(JsonObject input, String key) {
        if (input.containsKey(key)) {
            return Optional.of(new SimpleEntry(key, input.getValue(key)));
        }

        return input.getMap().entrySet().stream()
            .filter(entry -> entry.getKey().toLowerCase().equals(key))
            .findAny();
    }

}
