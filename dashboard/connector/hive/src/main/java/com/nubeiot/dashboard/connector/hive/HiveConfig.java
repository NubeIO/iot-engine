package com.nubeiot.dashboard.connector.hive;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class HiveConfig implements IConfig {

    public static final String NAME = "__hive__";

    private String url = "jdbc:hive2://localhost:10000/filo_db";
    private String driver_class = "org.apache.hive.jdbc.HiveDriver";
    private String user = "root";
    private String password = "root";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return NubeConfig.AppConfig.class;
    }

}
