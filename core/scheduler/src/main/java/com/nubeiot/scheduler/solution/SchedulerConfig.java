package com.nubeiot.scheduler.solution;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;

import lombok.Getter;

@Getter
public class SchedulerConfig implements IConfig {

    private String address = "com.nubeiot.scheduler";

    @Override
    public String name() {
        return "__scheduler__";
    }

    @Override
    public Class<? extends IConfig> parent() {
        return AppConfig.class;
    }

}
