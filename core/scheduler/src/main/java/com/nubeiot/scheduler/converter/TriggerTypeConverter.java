package com.nubeiot.scheduler.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.scheduler.trigger.TriggerType;

public class TriggerTypeConverter extends AbstractEnumConverter<TriggerType> {

    @Override
    public Class<TriggerType> toType() {
        return TriggerType.class;
    }

    @Override
    protected TriggerType def() {
        return null;
    }

}
