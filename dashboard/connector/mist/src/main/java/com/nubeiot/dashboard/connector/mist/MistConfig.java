package com.nubeiot.dashboard.connector.mist;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class MistConfig implements IConfig {

    public static final String NAME = "__mist__";

    private String schema = "http";
    private String host = "0.0.0.0";
    private int port = 8080;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return NubeConfig.AppConfig.class;
    }

}
