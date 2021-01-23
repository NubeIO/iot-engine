package com.nubeiot.dashboard.connector.postgresql;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PostgreSqlConfig implements IConfig {

    public static final String NAME = "__pg__";

    private String host = "0.0.0.0";
    private int port = 5432;
    private int maxPoolSize = 10;
    private String username = "root";
    private String password = "root";
    private String database = "test";
    private String charset = "UTF-8";
    private int queryTimeout = 10000;

    @Override
    public String key() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return NubeConfig.AppConfig.class;
    }

}
