package com.nubeiot.edge.connector.datapoint;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;

import lombok.Getter;

@Getter
public final class DataPointConfig implements IConfig {

    private boolean enabledLowDb = false;
    private String lowDbPath;

    @Override
    public String name() {
        return "__datapoint__";
    }

    @Override
    public Class<? extends IConfig> parent() {
        return AppConfig.class;
    }

}
