package com.nubeiot.iotdata.converter;

import java.time.DayOfWeek;
import java.util.Locale;
import java.util.Objects;

import io.github.zero.utils.Strings;

import com.nubeiot.core.sql.converter.ArrayConverter;

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
