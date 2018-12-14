package com.nubeiot.core.component;

import com.nubeiot.core.utils.Configs;

import io.vertx.core.json.JsonObject;

public interface IComponentProvider {

    static JsonObject computeConfig(String defCfgClasspathFile, String cfgPropertyName, JsonObject rootCfg) {
        JsonObject defMicroCfg = Configs.getSystemCfg(Configs.loadDefaultConfig(defCfgClasspathFile))
                                        .getJsonObject(cfgPropertyName, new JsonObject());
        JsonObject inputMicroCfg = Configs.getSystemCfg(rootCfg).getJsonObject(cfgPropertyName, new JsonObject());
        return defMicroCfg.mergeIn(inputMicroCfg, true);
    }

}
