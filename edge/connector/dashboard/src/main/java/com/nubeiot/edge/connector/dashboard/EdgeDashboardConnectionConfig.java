package com.nubeiot.edge.connector.dashboard;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@JsonNaming(value = SnakeCaseStrategy.class)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class EdgeDashboardConnectionConfig implements IConfig {

    public static final String NAME = "__edge_dashboard_connection__";

    private String gatewaySchema = "http";
    private String gatewayHost = "localhost";
    private int gatewayPort = 8080;
    private String gatewayApi = "/gw";
    private String gatewayRootApi = "/api";
    private String noderedSchema = "http";
    private String noderedHost = "localhost";
    private int noderedPort = 1880;

    @Override
    public String key() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

}

