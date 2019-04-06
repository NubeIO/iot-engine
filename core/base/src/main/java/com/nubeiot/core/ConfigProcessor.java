package com.nubeiot.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.NubeConfig.DeployConfig;
import com.nubeiot.core.NubeConfig.SystemConfig;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;

public class ConfigProcessor {

    private static final String NUBEIO = "NUBEIO";
    private static final String APP = "app";
    private static final String SYSTEM = "system";
    private static final String DEPLOY = "deploy";
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
        if (isBlank(key)) {
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

    private static boolean isBlank(String key) {
        return Strings.isBlank(key);
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
        //TODO sorting
        return result;
    }

    public <T extends IConfig> Optional<T> processAndOverride(@NonNull Class<T> clazz, IConfig provideConfig,
                                                              IConfig defaultConfig) {
        Map<String, Object> envConfig = this.mergeEnvAndSys();
        T input;
        if (Objects.isNull(provideConfig)) {
            input = (T) defaultConfig;
        } else if (Objects.isNull(defaultConfig)) {
            input = (T) provideConfig;
        } else {
            input = IConfig.merge(defaultConfig, provideConfig, clazz);
        }
        return overrideConfig(clazz, envConfig, input);
    }

    private <T extends IConfig> Optional<T> overrideConfig(Class<T> clazz, Map<String, Object> envConfig,
                                                           IConfig input) {
        T finalAppConfig;
        try {
            finalAppConfig = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return Optional.empty();
        }
        JsonObject nubeConfig = new JsonObject();

        JsonObject appConfig = new JsonObject();
        JsonObject systemConfig = new JsonObject();
        JsonObject deployConfig = new JsonObject();
        nubeConfig.put(AppConfig.NAME, appConfig);
        JsonObject inputAppConfig = input.toJson().getJsonObject(AppConfig.NAME);
        JsonObject inputSystemConfig = input.toJson().getJsonObject(SystemConfig.NAME);
        JsonObject inputDeployConfig = input.toJson().getJsonObject(DeployConfig.NAME);
        for (Map.Entry<String, Object> entry : envConfig.entrySet()) {
            String[] keyArray = entry.getKey().split("\\.");

            if (keyArray.length < 3) {
                continue;
            }
            switch (keyArray[1]) {
                case APP:
                    handleConfig(appConfig, inputAppConfig, entry, keyArray);
                    break;
                case SYSTEM:
                    handleConfig(systemConfig, inputSystemConfig, entry, keyArray);
                    break;
                case DEPLOY:
                    handleConfig(deployConfig, inputDeployConfig, entry, keyArray);
                    break;
                default:
                    //TODO case : dataDir
            }
        }
        //TODO merge to NubeConfig class
        System.out.println(appConfig);
        System.out.println(systemConfig);
        System.out.println(deployConfig);
        System.out.println(nubeConfig);
        return Optional.of(finalAppConfig);
    }

    private void handleConfig(JsonObject appConfig, JsonObject inputAppConfig, Entry<String, Object> entry,
                              String[] keyArray) {
        if (Objects.isNull(inputAppConfig)) {
            return;
        }
        String propertyName = keyArray[2];
        if (Objects.nonNull(inputAppConfig.getValue(propertyName))) {
            this.handleChildElement(appConfig, inputAppConfig, entry, keyArray, propertyName);
        } else if (Objects.nonNull(inputAppConfig.getValue("__" + propertyName + "__"))) {
            this.handleChildElement(appConfig, inputAppConfig, entry, keyArray, "__" + propertyName + "__");
        } else {
            this.handleChildElement(appConfig, inputAppConfig, entry, keyArray, propertyName);
        }
    }

    private void handleChildElement(JsonObject appConfig, JsonObject inputAppConfig, Entry<String, Object> entry,
                                    String[] keyArray, String temp) {
        Optional<JsonObject> entries = checkAndUpdate(3, keyArray,
                                                      entry.getValue().toString(),
                                                      inputAppConfig);
        if (!entries.isPresent()) {
            return;
        }
        JsonObject childElement = appConfig.getJsonObject(temp);
        if (Objects.isNull(childElement)) {
            appConfig.put(temp, entries.get());
        } else {
            childElement = childElement.mergeIn(entries.get());
        }
    }

    private boolean hasMatchChildConfig(String[] array, JsonObject input) {
        for (String item : Arrays.stream(array).skip(2).collect(Collectors.toList())) {
            if (input.getValue(item) != null || input.getValue("__" + item + "__") != null) {
                return true;
            }
        }
        return false;
    }

    private JsonObject get(String item, JsonObject jsonObject) {
        return new JsonObject().put(item, jsonObject);
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

    private Optional<JsonObject> checkAndUpdate(int index, String[] array, String overrideValue, JsonObject input)
        throws ClassCastException {
        // TODO remove Optional
        String key = array[index];
        Object value = input.getValue(key);
        if (Objects.nonNull(value)) {
            if (JsonObject.class.isInstance(value)) {
                Optional<JsonObject> temp = checkAndUpdate(index + 1, array, overrideValue, (JsonObject) value);
                if (temp.isPresent()) {
                    return Optional.of(new JsonObject().put(key, temp.get()));
                }
                return Optional.empty();
            }
            if (index == array.length - 1) {
                return Optional.of(new JsonObject().put(key, overrideValue));
            }
            return Optional.empty();
        } else if (Objects.nonNull(input.getValue("__" + key + "__"))) {
            value = input.getValue("__" + key + "__");
            if (JsonObject.class.isInstance(value)) {
                Optional<JsonObject> temp = checkAndUpdate(index + 1, array, overrideValue, (JsonObject) value);
                if (temp.isPresent()) {
                    return Optional.of(new JsonObject().put("__" + key + "__",
                                                            temp.get()));
                }
                ;
                return Optional.empty();
            }
            return Optional.empty();
        } else {
            //if input Value doesn't contains this attribute.
            if (index == array.length - 1) {
                return Optional.of(new JsonObject().put(key, overrideValue));
            }
            Optional<JsonObject> temp = checkAndUpdate(index + 1, array, overrideValue, new JsonObject());
            if (temp.isPresent()) {
                return Optional.of(new JsonObject().put(key, temp.get()));
            }
        }

        return Optional.empty();
    }

}
