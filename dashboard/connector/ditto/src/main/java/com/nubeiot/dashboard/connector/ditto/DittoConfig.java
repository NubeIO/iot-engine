package com.nubeiot.dashboard.connector.ditto;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DittoConfig implements IConfig {

    public static final String NAME = "__ditto__";

    private String host = "0.0.0.0";
    private int port = 8080;
    private String username = "ditto";
    private String password = "ditto";
    private Boolean policy = false;
    private String prefix = "io.nubeio";

    @Override
    public String key() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return NubeConfig.AppConfig.class;
    }

}
