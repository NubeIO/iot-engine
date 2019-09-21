package com.nubeiot.dashboard.connector.zeppelin;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;

public class ZeppelinConfig implements IConfig {

    public static final String NAME = "__zeppelin__";

    private String host = "0.0.0.0";
    private int port = 18080;

    @Override
    public String key() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return NubeConfig.AppConfig.class;
    }

}
