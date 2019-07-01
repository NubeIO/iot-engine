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
    private final Vertx vertx;
    private final LinkedHashMap<ConfigStoreOptions, Function<JsonObject, Map<String, Object>>> mappingOptions;

    public ConfigProcessor(Vertx vertx) {
        this.vertx = vertx;
        this.mappingOptions = new LinkedHashMap<>();
        initDefaultOptions();
    }

    public <T extends IConfig> Optional<T> processAndOverride(@NonNull Class<T> clazz, JsonObject defaultConfig,
                                                              JsonObject provideConfig, boolean overrideAppConfig,
                                                              boolean overrideOtherConfigs) {
        logger.info("Starting to override config");
        if ((Objects.isNull(provideConfig) && Objects.isNull(defaultConfig)) ||
            (!overrideAppConfig && !overrideOtherConfigs)) {
            return Optional.empty();
        }
        Map<String, Object> envConfig = this.mergeEnvVarAndSystemVar();
        return overrideConfig(clazz, envConfig, computeInputConfig(defaultConfig, provideConfig), overrideAppConfig,
                              overrideOtherConfigs);
    }

    private String convertEnvKey(String key) {
        if (Strings.isBlank(key)) {
            return key;
        }
        return key.toLowerCase(Locale.ENGLISH).replace('_', '.');
    }

    private Object convertEnvValue(Object value) {
        if (Objects.isNull(value)) {
            return "";
        }
        String strValue = value.toString();
        if (strValue.startsWith("[") && strValue.endsWith("]")) {
            return new ArrayList<>(Arrays.asList(strValue.substring(1, strValue.length() - 1).split(",")));
        }
        return value;
    }

    public Map<String, Object> mergeEnvVarAndSystemVar() {
        Map<String, Object> result = new HashMap<>();
        mappingOptions.forEach((store, filterNubeVariables) -> {
            ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(store);
            ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
            retriever.getConfig(json -> result.putAll(filterNubeVariables.apply(json.result())));
        });
        return new TreeMap<>(result);
    }

    private JsonObject computeInputConfig(JsonObject defaultConfig, JsonObject provideConfig) {
        if (Objects.isNull(provideConfig)) {
            return defaultConfig;
        }
        if (Objects.isNull(defaultConfig)) {
            return provideConfig;
        }
        JsonObject input = defaultConfig.mergeIn(provideConfig, true);
        if (logger.isDebugEnabled()) {
            logger.debug("Input NubeConfig: {}", input.encode());
        }
        return input;
    }

    private String convertDefaultKey(String key) {
        if (key.equalsIgnoreCase(NubeConfig.DATA_DIR)) {
            return NubeConfig.DATA_DIR;
        }
        if (key.startsWith("__") && key.endsWith("__")) {
            return key;
        }
        return "__" + key + "__";
    }

    private <T extends IConfig> Optional<T> overrideConfig(Class<T> clazz, Map<String, Object> envConfig,
                                                           JsonObject input, boolean overrideAppConfig,
                                                           boolean overrideSystemConfig) {
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
        Object inputDataDir = input.getValue(NubeConfig.DATA_DIR);

        for (Map.Entry<String, Object> entry : envConfig.entrySet()) {
            String[] keyArray = entry.getKey().split("\\.");
            if (keyArray.length < 1) {
                continue;
            }
            Object overrideValue = entry.getValue();
            final String defaultKey = convertDefaultKey(keyArray[1]);
            if (defaultKey.equals(AppConfig.NAME) && overrideAppConfig) {
                handleDomainConfig(appConfig, inputAppConfig, overrideValue, keyArray);
            }
            if (defaultKey.equals(SystemConfig.NAME) && overrideSystemConfig) {
                handleDomainConfig(systemConfig, inputSystemConfig, overrideValue, keyArray);
            }
            if (defaultKey.equals(DeployConfig.NAME) && overrideSystemConfig) {
                handleDomainConfig(deployConfig, inputDeployConfig, overrideValue, keyArray);
            }
            if (defaultKey.equals(NubeConfig.DATA_DIR) && overrideSystemConfig) {
                try {
                    nubeConfig.put(NubeConfig.DATA_DIR, FileUtils.toPath((String) overrideValue).toString());
                } catch (NubeException ex) {
                    logger.warn("DataDir is not valid. ", ex);
                }
            }
        }

        nubeConfig.put(AppConfig.NAME, new JsonObject(inputAppConfig.toString()).mergeIn(appConfig, true));
        nubeConfig.put(SystemConfig.NAME, new JsonObject(inputSystemConfig.toString()).mergeIn(systemConfig, true));
        nubeConfig.put(DeployConfig.NAME, new JsonObject(inputDeployConfig.toString()).mergeIn(deployConfig, true));

        if (!nubeConfig.containsKey(NubeConfig.DATA_DIR)) {
            nubeConfig.put(NubeConfig.DATA_DIR, inputDataDir);
        }
        try {
            return Optional.of(IConfig.MAPPER_IGNORE_UNKNOWN_PROPERTY.readValue(nubeConfig.encode(), clazz));
        } catch (IOException ex) {
            throw new NubeException("Converting to object failed", ex);
        }
    }

    private void handleDomainConfig(JsonObject domainConfig, JsonObject inputConfig, Object overrideValue,
                                    String[] keyArray) {
        if (Objects.isNull(inputConfig)) {
            return;
        }
        if (keyArray.length < 3) {
            return;
        }
        String propertyName = keyArray[2];
        Entry<String, Object> keyValue = extractKeyValue(inputConfig, propertyName);
        final Object value = this.handleChildrenConfig(domainConfig.getValue(keyValue.getKey()), overrideValue,
                                                       keyArray, keyValue);
        if (Objects.nonNull(value)) {
            domainConfig.put(keyValue.getKey(), value);
        }
    }

    private Entry<String, Object> extractKeyValue(JsonObject inputConfig, String propertyName) {
        Optional<Entry<String, Object>> valueKey = this.getValueByKey(inputConfig, propertyName);
        return valueKey.orElseGet(() -> this.getValueByKey(inputConfig, convertDefaultKey(propertyName))
                                            .orElse(new SimpleEntry<>(propertyName, null)));
    }

    private Object handleChildrenConfig(Object destConfig, Object overrideValue, String[] keyArray,
                                        Entry<String, Object> keyValue) {
        Object value = keyValue.getValue();
        value = keyArray.length < 4 ? value : Objects.isNull(value) ? new JsonObject() : value;
        if (keyArray.length < 4) {
            return this.getOverrideProperty(overrideValue, value);
        }
        if (value instanceof JsonObject || value instanceof Map) {
            final JsonObject v = value instanceof JsonObject ? (JsonObject) value : JsonObject.mapFrom(value);
            JsonObject overrideResult = checkAndUpdate(3, keyArray, overrideValue, v);
            if (Objects.nonNull(destConfig) && Objects.nonNull(overrideResult)) {
                JsonObject childElement = (JsonObject) destConfig;
                overrideResult.getMap().forEach(childElement::put);
            }
            return overrideResult;
        }
        if (value instanceof JsonArray || value instanceof Collection) {
            JsonArray jsonArray = value instanceof JsonArray
                                  ? (JsonArray) value
                                  : new JsonArray(new ArrayList<>((Collection) value));
            if (!jsonArray.isEmpty()) {
                Object item = jsonArray.getList().get(0);
                if (item instanceof JsonObject || item instanceof Map) {
                    //not supported yet
                    return null;
                }
            }
            if (keyArray.length > 3) {
                return null;
            }
            value = jsonArray.getList();
        }
        return this.getOverrideProperty(overrideValue, value);
    }

    private void initDefaultOptions() {
        initConfig("sys", NUBEIO_SYS.toLowerCase());
        initConfig("env", NUBEIO_ENV);
    }

    private void initConfig(String type, String prefixKey) {
        ConfigStoreOptions store = new ConfigStoreOptions().setType(type).setOptional(true);
        mappingOptions.put(store, entries -> entries.stream()
                                                    .filter(x -> x.getKey().startsWith(prefixKey))
                                                    .collect(Collectors.toMap(e -> convertEnvKey(e.getKey()),
                                                                              e -> convertEnvValue(e.getValue()))));
    }

    private JsonObject checkAndUpdate(int index, String[] array, Object overrideValue, JsonObject input) {
        return checkingNextLevel(index, array, overrideValue, extractKeyValue(input, array[index]));
    }

    private JsonObject checkingNextLevel(int index, String[] array, Object overrideValue, Entry<String, Object> entry) {
        Object value = entry.getValue();
        if (index == array.length - 1) {
            Object overrideProperty = this.getOverrideProperty(overrideValue, value);
            if (Objects.isNull(overrideProperty)) {
                return null;
            }
            return new JsonObject().put(entry.getKey(), overrideProperty);
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
                                  value instanceof JsonObject ? (JsonObject) value : JsonObject.mapFrom(value));
        } else {
            return null;
        }
        if (Objects.nonNull(temp)) {
            return new JsonObject().put(entry.getKey(), temp);
        }
        return null;
    }

    private Object getOverrideProperty(Object overrideValue, Object value) {
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
            logger.warn("Invalid data type. Cannot cast from {} to {}", ex, overrideValue, value.getClass().getName());
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
            return Optional.of(new SimpleEntry<>(key, input.getValue(key)));
        }

        return input.getMap()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().toLowerCase().equals(key) && Objects.nonNull(entry.getValue()))
                    .findAny();
    }

}
