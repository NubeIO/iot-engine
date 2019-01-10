package com.nubeiot.core.kafka;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;

public final class KafkaConfig implements IConfig {

    @Override
    public String name() {
        return "__kafka__";
    }

    @Override
    public Class<? extends IConfig> parent() {
        return NubeConfig.AppConfig.class;
    }

}
