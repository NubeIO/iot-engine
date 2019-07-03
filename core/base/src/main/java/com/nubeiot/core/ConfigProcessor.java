package com.nubeiot.core;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
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
import com.nubeiot.core.NubeConfig.SecretConfig;
import com.nubeiot.core.NubeConfig.SystemConfig;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public final class ConfigProcessor {

    private static final int DOMAIN_CFG_INDEX = 2;
    private static final int CHILD_CFG_START_INDEX = 3;
    private static final Logger logger = LoggerFactory.getLogger(ConfigProcessor.class);
    private static final String NUBEIO_SYS = "NUBEIO.";
    private static final String NUBEIO_ENV = "NUBEIO_";
    private final Vertx vertx;
    private final LinkedHashMap<ConfigStoreOptions, Function<JsonObject, Map<String, Object>>> mappingOptions;

    public ConfigProcessor(Vertx vertx) {
        this.vertx = vertx;
        this.mappingOptions = new LinkedHashMap<>();
        initConfig("sys", NUBEIO_SYS.toLowerCase());
        initConfig("env", NUBEIO_ENV);
    }

    public ConfigProcessor(io.vertx.reactivex.core.Vertx vertx) {
        this(vertx.getDelegate());
    }

    private static JsonObject toJson(Object configValue) {
        return configValue instanceof JsonObject
               ? (JsonObject) configValue
               : configValue instanceof Map ? JsonObject.mapFrom(configValue) : new JsonObject();
    }

    private static boolean isNotSupportedInArray(JsonArray jsonArray) {
        if (Objects.isNull(jsonArray)) {
            return true;
        }
        if (jsonArray.isEmpty()) {
            return false;
        }
        return isJsonable(jsonArray.getList().get(0));
    }

    private static boolean isJsonable(Object object) {
        return object instanceof JsonObject || object instanceof Map;
    }

    private static boolean isArrayable(Object object) {
        return object instanceof JsonArray || object instanceof Collection;
    }

    public Optional<NubeConfig> override(JsonObject fileConfig, boolean overrideAppConfig,
                                         boolean overrideOtherConfigs) {
        logger.info("Starting to override config");
        if (Objects.isNull(fileConfig) || !overrideAppConfig && !overrideOtherConfigs) {
            return Optional.empty();
        }
        return overrideConfig(mergeEnvVarAndSystemVar(), fileConfig, overrideAppConfig, overrideOtherConfigs);
    }

    Optional<NubeConfig> override(JsonObject defaultConfig, JsonObject provideConfig, boolean overrideAppConfig,
                                  boolean overrideOtherConfigs) {
        if ((Objects.isNull(provideConfig) && Objects.isNull(defaultConfig))) {
            return Optional.empty();
        }
        return override(fileConfig(defaultConfig, provideConfig), overrideAppConfig, overrideOtherConfigs);
    }

    private void initConfig(String type, String prefixKey) {
        ConfigStoreOptions store = new ConfigStoreOptions().setType(type).setOptional(true);
        mappingOptions.put(store, entries -> entries.stream()
                                                    .filter(x -> x.getKey().startsWith(prefixKey) &&
                                                                 Objects.nonNull(x.getValue()))
                                                    .collect(Collectors.toMap(e -> convertEnvKey(e.getKey()),
                                                                              e -> convertEnvValue(e.getValue()))));
    }

    SortedMap<String, Object> mergeEnvVarAndSystemVar() {
        SortedMap<String, Object> result = new TreeMap<>();
        mappingOptions.forEach((store, filterNubeVariables) -> {
            ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(store);
            ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
            retriever.getConfig(json -> result.putAll(filterNubeVariables.apply(json.result())));
        });
        return result;
    }

    private String convertEnvKey(String key) {
        return Strings.isBlank(key) ? key : key.toLowerCase(Locale.ENGLISH).replace('_', '.');
    }

    private Object convertEnvValue(Object value) {
        String strValue = value.toString();
        if (strValue.startsWith("[") && strValue.endsWith("]")) {
            return new ArrayList<>(Arrays.asList(strValue.substring(1, strValue.length() - 1).split(",")));
        }
        return value;
    }

    private JsonObject fileConfig(JsonObject defaultConfig, JsonObject provideConfig) {
        if (Objects.isNull(provideConfig)) {
            return defaultConfig;
        }
        if (Objects.isNull(defaultConfig)) {
            return provideConfig;
        }
        JsonObject input = defaultConfig.mergeIn(provideConfig, true);
        if (logger.isDebugEnabled()) {
            JsonObject inputCopy = input.copy();
            input.remove(SecretConfig.NAME);
            logger.debug("Input NubeConfig: {}", inputCopy.encode());
        }
        return input;
    }

    private String toStandardKey(String key) {
        if (key.equalsIgnoreCase(NubeConfig.DATA_DIR)) {
            return NubeConfig.DATA_DIR;
        }
        if (key.startsWith("__") && key.endsWith("__")) {
            return key;
        }
        return "__" + key + "__";
    }

    private Optional<NubeConfig> overrideConfig(Map<String, Object> envConfig, JsonObject fileConfig,
                                                boolean overrideAppConfig, boolean overrideSystemConfig) {
        JsonObject nubeConfig = new JsonObject();
        JsonObject inputAppConfig = fileConfig.getJsonObject(AppConfig.NAME, new JsonObject());
        JsonObject inputSecretConfig = fileConfig.getJsonObject(SecretConfig.NAME, new JsonObject());
        JsonObject inputSystemConfig = fileConfig.getJsonObject(SystemConfig.NAME, new JsonObject());
        JsonObject inputDeployConfig = fileConfig.getJsonObject(DeployConfig.NAME, new JsonObject());
        JsonObject destAppConfig = new JsonObject();
        JsonObject destSecretConfig = new JsonObject();
        JsonObject destSystemConfig = new JsonObject();
        JsonObject destDeployConfig = new JsonObject();
        Object inputDataDir = fileConfig.getValue(NubeConfig.DATA_DIR);

        for (Entry<String, Object> entry : envConfig.entrySet()) {
            String[] envKeyParts = entry.getKey().split("\\.");
            if (envKeyParts.length < DOMAIN_CFG_INDEX) {
                continue;
            }
            Object envValue = entry.getValue();
            String standardKey = toStandardKey(envKeyParts[1]);
            if (standardKey.equals(NubeConfig.DATA_DIR) && overrideSystemConfig) {
                try {
                    nubeConfig.put(NubeConfig.DATA_DIR, FileUtils.toPath((String) envValue).toString());
                } catch (NubeException ex) {
                    logger.warn("DataDir is not valid. ", ex);
                }
            }
            if (standardKey.equals(SystemConfig.NAME) && overrideSystemConfig) {
                handleDomainConfig(destSystemConfig, inputSystemConfig, envValue, envKeyParts);
            }
            if (standardKey.equals(DeployConfig.NAME) && overrideSystemConfig) {
                handleDomainConfig(destDeployConfig, inputDeployConfig, envValue, envKeyParts);
            }
            if (standardKey.equals(AppConfig.NAME) && overrideAppConfig) {
                handleDomainConfig(destAppConfig, inputAppConfig, envValue, envKeyParts);
            }
            if (standardKey.equals(SecretConfig.NAME) && overrideAppConfig) {
                handleDomainConfig(destSecretConfig, inputSecretConfig, envValue, envKeyParts);
            }
        }

        nubeConfig.put(AppConfig.NAME, new JsonObject(inputAppConfig.toString()).mergeIn(destAppConfig, true));
        nubeConfig.put(SecretConfig.NAME, new JsonObject(inputSecretConfig.toString()).mergeIn(destSecretConfig, true));
        nubeConfig.put(SystemConfig.NAME, new JsonObject(inputSystemConfig.toString()).mergeIn(destSystemConfig, true));
        nubeConfig.put(DeployConfig.NAME, new JsonObject(inputDeployConfig.toString()).mergeIn(destDeployConfig, true));

        if (!nubeConfig.containsKey(NubeConfig.DATA_DIR)) {
            nubeConfig.put(NubeConfig.DATA_DIR, inputDataDir);
        }
        try {
            return Optional.of(IConfig.MAPPER_IGNORE_UNKNOWN_PROPERTY.readValue(nubeConfig.encode(), NubeConfig.class));
        } catch (IOException ex) {
            throw new NubeException("Converting to object failed", ex);
        }
    }

    private void handleDomainConfig(JsonObject destConfig, JsonObject configFromFile, Object envValue,
                                    String[] envKeyParts) {
        Entry<String, Object> configValue = extractKeyValue(configFromFile, envKeyParts[DOMAIN_CFG_INDEX]);
        handleDomainChildConfig(envValue, envKeyParts, configValue).ifPresent(o -> {
            if (o instanceof JsonObject) {
                destConfig.put(configValue.getKey(), destConfig.getJsonObject(configValue.getKey(), new JsonObject())
                                                               .mergeIn((JsonObject) o, true));
            } else {
                destConfig.put(configValue.getKey(), o);
            }
        });
    }

    private Entry<String, Object> extractKeyValue(JsonObject config, String propertyName) {
        return this.getValueByKey(config, propertyName)
                   .orElseGet(() -> this.getValueByKey(config, toStandardKey(propertyName))
                                        .orElse(new SimpleEntry<>(propertyName, null)));
    }

    private Optional<Object> handleDomainChildConfig(Object envValue, String[] envKeyParts,
                                                     Entry<String, Object> configKeyValue) {
        Object configValue = configKeyValue.getValue();
        if (envKeyParts.length == CHILD_CFG_START_INDEX) {
            return Optional.ofNullable(this.overrideValue(envValue, configValue));
        }
        if (isArrayable(configValue)) {
            return Optional.empty();
        }
        if (Objects.nonNull(configValue) && !isJsonable(configValue)) {
            return Optional.ofNullable(this.overrideValue(envValue, configValue));
        }
        Entry<String, Object> configByIndex = extractKeyValue(toJson(configValue), envKeyParts[CHILD_CFG_START_INDEX]);
        return scanNextLevel(CHILD_CFG_START_INDEX, envKeyParts, envValue, configByIndex).map(
            o -> new JsonObject().put(configByIndex.getKey(), o));
    }

    private Optional<Object> scanNextLevel(int index, String[] envKeyParts, Object envValue,
                                           Entry<String, Object> configKeyValue) {
        Object configValue = configKeyValue.getValue();
        if (index == envKeyParts.length - 1) {
            return Optional.ofNullable(overrideValue(envValue, configValue));
        }
        if (Objects.isNull(configValue)) {
            Entry<String, Object> configByIndex = extractKeyValue(new JsonObject(), envKeyParts[index + 1]);
            return scanNextLevel(index + 1, envKeyParts, envValue, configByIndex).map(
                o -> new JsonObject().put(configByIndex.getKey(), o));
        }
        if (isArrayable(configValue)) {
            return Optional.ofNullable(toArray(configValue));
        }
        if (isJsonable(configValue)) {
            return scanNextLevel(index + 1, envKeyParts, envValue,
                                 extractKeyValue(toJson(configValue), envKeyParts[index + 1]));
        }
        return Optional.empty();
    }

    private JsonArray toArray(Object value) {
        @SuppressWarnings("unchecked")
        JsonArray array = value instanceof JsonArray
                          ? (JsonArray) value
                          : new JsonArray(new ArrayList((Collection) value));
        return isNotSupportedInArray(array) ? null : array;
    }

    private Object overrideValue(@NonNull Object envValue, Object configValue) {
        try {
            if (Objects.isNull(configValue)) {
                return envValue;
            }
            Number number = overrideNumberValue(envValue, configValue);
            if (Objects.nonNull(number)) {
                return number;
            }

            if (configValue instanceof JsonArray) {
                return ((JsonArray) configValue).getList().getClass().cast(envValue);
            }

            return configValue.getClass().cast(envValue);
        } catch (ClassCastException ex) {
            logger.warn("Invalid data type. Cannot cast from {} to {}", ex, envValue, configValue.getClass().getName());
            return null;
        }
    }

    private Number overrideNumberValue(@NonNull Object envValue, @NonNull Object configValue) {
        if (configValue instanceof Integer && envValue instanceof Number) {
            logger.warn("Source data type is Integer but input is Number");
            return ((Number) envValue).intValue();
        }

        if (configValue instanceof Double && envValue instanceof Number) {
            logger.warn("Source data type is Double but input is Number");
            return ((Number) envValue).doubleValue();
        }

        if (configValue instanceof Float && envValue instanceof Number) {
            logger.warn("Source data type is Float but input is Number");
            return ((Number) envValue).floatValue();
        }

        if (configValue instanceof Long && envValue instanceof Number) {
            logger.warn("Source data type is Long but input is Number");
            return ((Number) envValue).longValue();
        }

        if (configValue instanceof Short && envValue instanceof Number) {
            logger.warn("Source data type is Short but input is Number");
            return ((Number) envValue).shortValue();
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
