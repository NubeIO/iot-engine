package com.nubeiot.core.component;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.utils.Configs;

import io.vertx.core.json.JsonObject;

public interface IComponentProvider {

    static JsonObject computeConfig(String defCfgClasspathFile, String cfgPropertyName, JsonObject rootCfg) {
        JsonObject jsonConfig = Configs.loadJsonConfig(defCfgClasspathFile);
        NubeConfig.SystemConfig defSysCfg = IConfig.from(jsonConfig, NubeConfig.SystemConfig.class);
        JsonObject inputCfg = IConfig.from(rootCfg, NubeConfig.SystemConfig.class)
                                     .toJson()
                                     .getJsonObject(cfgPropertyName, new JsonObject());
        return defSysCfg.toJson().mergeIn(inputCfg, true);
    }

    static <T extends IConfig> T computeConfig(String defCfgClasspathFile, Class<T> configClass, JsonObject config) {
        T defaultCfg = IConfig.from(Configs.loadJsonConfig(defCfgClasspathFile), configClass);
        T givenCfg = IConfig.from(config, configClass);
        return IConfig.merge(defaultCfg, givenCfg);
    }

    static <T extends IConfig> T computeConfig(String defCfgClasspathFile, Class<T> configClass, NubeConfig config) {
        T defaultCfg = IConfig.from(Configs.loadJsonConfig(defCfgClasspathFile), configClass);
        T givenCfg = IConfig.from(config, configClass);
        return IConfig.merge(defaultCfg, givenCfg);
    }

    static <T extends IConfig> T computeConfig(String defCfgClasspathFile, Class<T> configClass, T config) {
        T defaultCfg = IConfig.from(Configs.loadJsonConfig(defCfgClasspathFile), configClass);
        return IConfig.merge(defaultCfg, config);
    }

}
