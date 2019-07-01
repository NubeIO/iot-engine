package com.nubeiot.scheduler;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class SchedulerConfig implements IConfig {

    @NonNull
    private String address = "com.nubeiot.scheduler";

    @Override
    public String name() { return "__scheduler__"; }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

}
