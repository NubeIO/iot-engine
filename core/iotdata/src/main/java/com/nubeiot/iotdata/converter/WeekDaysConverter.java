package com.nubeiot.iotdata.converter;

import java.time.DayOfWeek;
import java.util.Locale;
import java.util.Objects;

import com.nubeiot.core.sql.converter.ArrayConverter;
import com.nubeiot.core.utils.Strings;

public final class WeekDaysConverter extends ArrayConverter<DayOfWeek> {

    @Override
    public DayOfWeek parse(Object object) {
        if (Objects.isNull(object)) {
            return null;
        }
        return DayOfWeek.valueOf(Strings.toString(object).toUpperCase(Locale.ENGLISH));
    }

    @Override
    public Class<DayOfWeek> itemClass() {
        return DayOfWeek.class;
    }

}
