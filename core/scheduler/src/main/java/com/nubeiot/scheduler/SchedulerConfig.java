package com.nubeiot.scheduler;

import java.util.UUID;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SchedulerConfig implements IConfig {

    private static final String BASE_ADDRESS = "com.nubeiot.scheduler";
    private String address = BASE_ADDRESS + "." + UUID.randomUUID().toString();

    @Override
    public String name() { return "__scheduler__"; }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

}
