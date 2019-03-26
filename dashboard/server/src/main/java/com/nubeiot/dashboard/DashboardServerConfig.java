package com.nubeiot.dashboard;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class DashboardServerConfig implements IConfig {

    public static final String NAME = "__dashboardServer__";

    private String mediaPath = "media";
    private Boolean dittoPolicy = false;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() {
        return NubeConfig.AppConfig.class;
    }

}
