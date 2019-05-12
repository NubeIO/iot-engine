package com.nubeiot.core;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

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

    private Object convertEnvValue(Object value) {
        if (Objects.isNull(value)) {
            return value;
        }
        String strValue = value.toString();
        if (strValue.startsWith("[") && strValue.endsWith("]")) {
            return new ArrayList(Arrays.asList(strValue.substring(1, strValue.length() - 1).split(",")));
        }
        return value;
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

    public <T extends IConfig> Optional<T> processAndOverride(@NonNull Class<T> clazz, JsonObject defaultConfig,
                                                              JsonObject provideConfig, boolean overrideAppConfig,
                                                              boolean overrideOtherConfigs) {
        logger.info("Starting to override config");
        if ((Objects.isNull(provideConfig) && Objects.isNull(defaultConfig)) ||
            (!overrideAppConfig && !overrideOtherConfigs)) {
            return Optional.empty();
        }
        Map<String, Object> envConfig = this.mergeEnvAndSys();
        JsonObject input;
        if (Objects.isNull(provideConfig)) {
            logger.debug("Provide config is null");
            input = defaultConfig;
        } else if (Objects.isNull(defaultConfig)) {
            logger.debug("Default config is null");
            input = provideConfig;
        } else {
            input = defaultConfig.mergeIn(provideConfig, true);
        }
        logger.info("Input Nubeconfig: {}", input.toString());
        return overrideConfig(clazz, envConfig, input, overrideAppConfig, overrideOtherConfigs);
    }

    private <T extends IConfig> Optional<T> overrideConfig(Class<T> clazz, Map<String, Object> envConfig,
                                                           JsonObject input, boolean overrideAppConfig,
                                                           boolean overrideOtherConfigs) {
        JsonObject nubeConfig = new JsonObject();

        JsonObject inputAppConfig = input.getJsonObject(AppConfig.NAME);
        if (Objects.isNull(inputAppConfig)) {
            inputAppConfig = new JsonObject();
        }
        JsonObject inputSystemConfig = input.getJsonObject(SystemConfig.NAME);
        if (Objects.isNull(inputSystemConfig)) {
            inputSystemConfig = new JsonObject();
        }
        JsonObject inputDeployConfig = input.getJsonObject(DeployConfig.NAME);
        if (Objects.isNull(inputDeployConfig)) {
            inputDeployConfig = new JsonObject();
        }
        JsonObject appConfig = new JsonObject();
        JsonObject systemConfig = new JsonObject();
        JsonObject deployConfig = new JsonObject();
        Object inputDataDir = input.getValue(DATA_DIR);

        for (Map.Entry<String, Object> entry : envConfig.entrySet()) {
            String[] keyArray = entry.getKey().split("\\.");
            Object overrideValue = entry.getValue();
            //TODO should be able to handle the generic case
            switch (keyArray[1]) {
                case APP:
                    if (overrideAppConfig) {
                        handleConfig(appConfig, inputAppConfig, overrideValue, keyArray);
                    }
                    break;
                case SYSTEM:
                    if (overrideOtherConfigs) {
                        handleConfig(systemConfig, inputSystemConfig, overrideValue, keyArray);
                    }
                    break;
                case DEPLOY:
                    if (overrideOtherConfigs) {
                        handleConfig(deployConfig, inputDeployConfig, overrideValue, keyArray);
                    }
                    break;
                case DATA_DIR_LOWER_CASE:
                    if (overrideOtherConfigs) {
                        try {
                            if (!(overrideValue instanceof String)) {
                                continue;
                            }
                            FileUtils.toPath(overrideValue.toString());
                        } catch (NubeException ex) {
                            logger.warn("DataDir is not valid. ", ex);
                            continue;
                        }
                        nubeConfig.put(DATA_DIR, overrideValue);
                    }
                    break;
            }
        }

        nubeConfig.put(AppConfig.NAME, new JsonObject(inputAppConfig.toString()).mergeIn(appConfig, true));
        nubeConfig.put(SystemConfig.NAME, new JsonObject(inputSystemConfig.toString()).mergeIn(systemConfig, true));
        nubeConfig.put(DeployConfig.NAME, new JsonObject(inputDeployConfig.toString()).mergeIn(deployConfig, true));

        if (!nubeConfig.containsKey(DATA_DIR)) {
            nubeConfig.put(DATA_DIR, inputDataDir);
        }
        try {
            return Optional.of(IConfig.MAPPER_IGNORE_UNKNOWN_PROPERTY.readValue(nubeConfig.toString(), clazz));
        } catch (IOException ex) {
            logger.warn("Converting to object failed", ex);
            throw new NubeException(ex);
        }
    }

    private void handleConfig(JsonObject config, JsonObject inputConfig, Object overrideValue, String[] keyArray) {
        if (Objects.isNull(inputConfig)) {
            return;
        }
        if (keyArray.length < 3) {
            return;
        }
        String propertyName = keyArray[2];
        Optional<Entry<String, Object>> valueByKey = this.getValueByKey(inputConfig, propertyName);
        if (valueByKey.isPresent() && Objects.nonNull(valueByKey.get().getValue())) {
            this.handleChildElement(config, overrideValue, keyArray, valueByKey.get());
            return;
        }

        valueByKey = this.getValueByKey(inputConfig, "__" + propertyName + "__");
        if (valueByKey.isPresent() && Objects.nonNull(valueByKey.get().getValue())) {
            this.handleChildElement(config, overrideValue, keyArray, valueByKey.get());
        } else {
            this.handleChildElement(config, overrideValue, keyArray,
                                    new SimpleEntry<String, Object>(propertyName, null));
        }
    }

    private void handleChildElement(JsonObject appConfig, Object overrideValue, String[] keyArray,
                                    Entry<String, Object> valueByKey) {
        Object value = valueByKey.getValue();

        if (keyArray.length < 4) {
            Object overridedProperty = this.getOverridedProperty(overrideValue, value);
            if (Objects.isNull(overridedProperty)) {
                return;
            }
            appConfig.put(valueByKey.getKey(), overridedProperty);
            return;
        }
        if (Objects.isNull(value)) {
            value = new JsonObject();
        }

        if (value instanceof JsonArray || value instanceof Collection) {
            JsonArray jsonArray = value instanceof JsonArray
                                  ? (JsonArray) value
                                  : new JsonArray(new ArrayList((Collection) value));
            if (!jsonArray.isEmpty()) {
                Object item = ((JsonArray) value).getList().get(0);
                if (item instanceof JsonObject || item instanceof Map) {
                    //not supported yet
                    return;
                }
            }
            if (keyArray.length > 3) {
                return;
            }
            value = jsonArray.getList();
        }

        if (value instanceof JsonObject || value instanceof Map) {
            JsonObject overrideResult = checkAndUpdate(3, keyArray, overrideValue, value instanceof JsonObject
                                                                                   ? (JsonObject) value
                                                                                   : JsonObject.mapFrom((Map) value));
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
            return;
        }

        Object overridedProperty = this.getOverridedProperty(overrideValue, value);
        if (Objects.isNull(overridedProperty)) {
            return;
        }
        appConfig.put(valueByKey.getKey(), this.getOverridedProperty(overrideValue, value));
    }

    private void initDefaultOptions() {
        initSystemOption();
        initEnvironmentOption();
    }

    private void initSystemOption() {
        ConfigStoreOptions systemStore = new ConfigStoreOptions().setType("sys").setOptional(true);
        mappingOptions.put(systemStore, entries -> entries.stream()
                                                          .filter(x -> x.getKey().startsWith(NUBEIO_SYS.toLowerCase()))
                                                          .collect(Collectors.toMap(entry -> convertEnv(entry.getKey()),
                                                                                    entry -> this.convertEnvValue(
                                                                                        entry.getValue()))));
    }

    private void initEnvironmentOption() {
        ConfigStoreOptions environmentStore = new ConfigStoreOptions().setType("env").setOptional(true);
        mappingOptions.put(environmentStore, entries -> entries.stream()
                                                               .filter(x -> x.getKey().startsWith(NUBEIO_ENV))
                                                               .collect(
                                                                   Collectors.toMap(entry -> convertEnv(entry.getKey()),
                                                                                    entry -> this.convertEnvValue(
                                                                                        entry.getValue()))));
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

    private JsonObject checkingNextLevel(int index, String[] array, Object overrideValue, Entry<String, Object> entry) {
        Object value = entry.getValue();
        if (index == array.length - 1) {
            Object overridedProperty = this.getOverridedProperty(overrideValue, value);
            if (Objects.isNull(overridedProperty)) {
                return null;
            }
            return new JsonObject().put(entry.getKey(), overridedProperty);
        }
        JsonObject temp;
        if (Objects.isNull(value)) {
            temp = checkAndUpdate(index + 1, array, overrideValue, new JsonObject());
        } else if (value instanceof JsonArray || value instanceof Collection) {
            JsonArray jsonArray = value instanceof JsonArray
                                  ? (JsonArray) value
                                  : new JsonArray(new ArrayList((Collection) value));
            if (!jsonArray.isEmpty()) {
                Object item = ((JsonArray) value).getList().get(0);
                if (item instanceof JsonObject || item instanceof Map) {
                    //not supported yet
                    return null;
                }
            }
            return null;
        } else if (value instanceof JsonObject || value instanceof Map) {
            temp = checkAndUpdate(index + 1, array, overrideValue,
                                  value instanceof JsonObject ? (JsonObject) value : JsonObject.mapFrom((Map) value));
        } else {
            return null;
        }
        if (Objects.nonNull(temp)) {
            return new JsonObject().put(entry.getKey(), temp);
        }
        return null;
    }

    private Object getOverridedProperty(Object overrideValue, Object value) {
        try {
            if (Objects.isNull(value)) {
                return overrideValue;
            }
            Number number = handleNumberClasses(overrideValue, value);
            if (Objects.nonNull(number)) {
                return number;
            }

            if (value instanceof JsonArray) {
                return ((JsonArray) value).getList().getClass().cast(overrideValue);
            }

            return value.getClass().cast(overrideValue);
        } catch (ClassCastException ex) {
            logger.warn("Invalid data type. Cannot cast from " + overrideValue + " to " + value.getClass().getName(),
                        ex);
            return null;
        }
    }

    private Number handleNumberClasses(Object overrideValue, Object value) {
        if (value instanceof Integer && overrideValue instanceof Number) {
            logger.warn("Source data type is Integer but input is Number");
            return ((Number) overrideValue).intValue();
        }

        if (value instanceof Double && overrideValue instanceof Number) {
            logger.warn("Source data type is Double but input is Number");
            return ((Number) overrideValue).doubleValue();
        }

        if (value instanceof Float && overrideValue instanceof Number) {
            logger.warn("Source data type is Float but input is Number");
            return ((Number) overrideValue).floatValue();
        }

        if (value instanceof Long && overrideValue instanceof Number) {
            logger.warn("Source data type is Long but input is Number");
            return ((Number) overrideValue).longValue();
        }

        if (value instanceof Short && overrideValue instanceof Number) {
            logger.warn("Source data type is Short but input is Number");
            return ((Number) overrideValue).shortValue();
        }

        return null;
    }

    private Optional<Entry<String, Object>> getValueByKey(JsonObject input, String key) {
        if (input.containsKey(key)) {
            return Optional.of(new SimpleEntry(key, input.getValue(key)));
        }

        return input.getMap().entrySet().stream().filter(entry -> entry.getKey().toLowerCase().equals(key)).findAny();
    }

}
