package com.nubeiot.core.kafka;

import com.nubeiot.core.component.UnitVerticle;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class KafkaUnit extends UnitVerticle<KafkaConfig> {

    @Override
    public Class<KafkaConfig> configClass() { return KafkaConfig.class; }

    @Override
    public String configFile() { return "kafka.json"; }

}
