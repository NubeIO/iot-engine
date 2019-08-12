package com.nubeiot.scheduler.trigger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;

public final class TriggerType extends AbstractEnumType {

    public static final TriggerType CRON = new TriggerType("CRON");
    public static final TriggerType PERIODIC = new TriggerType("PERIODIC");

    private TriggerType(String type) {
        super(type);
    }

    @JsonCreator
    public static TriggerType factory(String type) {
        return EnumType.factory(type, TriggerType.class);
    }

}
