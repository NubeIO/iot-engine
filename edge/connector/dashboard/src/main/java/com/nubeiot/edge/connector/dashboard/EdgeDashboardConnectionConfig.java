package com.nubeiot.edge.connector.dashboard;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class EdgeDashboardConnectionConfig implements IConfig {

    public static final String NAME = "__edge_dashboard_connection__";
    private String gateway_schema = "http";
    private String gateway_host = "localhost";
    private int gateway_port = 8080;
    private String gateway_root_api = "/api";
    private String edge_root_api = "/edge-api";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

}

