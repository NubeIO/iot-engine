package com.nubeiot.core.kafka;

import com.nubeiot.core.component.UnitProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class KafkaProvider implements UnitProvider<KafkaUnit> {

    private final KafkaRouter kafkaRouter;

    @Override
    public Class<KafkaUnit> unitClass() { return KafkaUnit.class; }

    @Override
    public KafkaUnit get() { return new KafkaUnit(kafkaRouter); }

}
