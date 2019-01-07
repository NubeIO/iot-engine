package com.nubeiot.core.kafka;

import com.nubeiot.core.component.UnitProvider;

public final class KafkaProvider implements UnitProvider<KafkaUnit> {

    @Override
    public Class<KafkaUnit> unitClass() { return KafkaUnit.class; }

    @Override
    public KafkaUnit get() { return new KafkaUnit(); }

}
