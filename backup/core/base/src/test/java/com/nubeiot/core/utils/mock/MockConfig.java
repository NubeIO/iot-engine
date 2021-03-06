package com.nubeiot.core.utils.mock;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;

import lombok.Getter;
import lombok.Setter;

public class MockConfig implements IConfig {

    @Setter
    @Getter
    private String name;

    @Override
    public String key() {
        return "mock";
    }

    @Override
    public Class<? extends IConfig> parent() {
        return AppConfig.class;
    }

}
