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

    static <T extends IConfig> T computeConfig(String defCfgClasspathFile, Class<T> configClass, JsonObject given) {
        return IConfig.merge(IConfig.from(Configs.loadJsonConfig(defCfgClasspathFile), configClass), given,
                             configClass);
    }

    static <T extends IConfig> T computeConfig(String defCfgClasspathFile, Class<T> configClass, NubeConfig config) {
        return IConfig.merge(Configs.loadJsonConfig(defCfgClasspathFile), IConfig.from(config, configClass),
                             configClass);
    }

    static <T extends IConfig> T computeConfig(String defCfgClasspathFile, Class<T> configClass, T config) {
        return IConfig.merge(Configs.loadJsonConfig(defCfgClasspathFile), config, configClass);
    }

}
