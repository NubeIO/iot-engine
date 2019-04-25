package com.nubeiot.edge.connector.datapoint.converter.model;

import com.nubeiot.core.sql.converter.ArrayConverter;
import com.nubeiot.edge.connector.datapoint.dto.WeekDay;

public final class WeekDaysConverter extends ArrayConverter<WeekDay> {

    @Override
    public WeekDay parse(Object object) {
        return WeekDay.valueOf(object.toString());
    }

    @Override
    public Class<WeekDay> itemClass() {
        return WeekDay.class;
    }

}
