package com.nubeiot.iotdata.converter;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.iotdata.unit.DataType;
import com.nubeiot.iotdata.unit.DataTypeCategory.All;

public final class DataTypeConverter implements Converter<String, DataType> {

    @Override
    public DataType from(String databaseObject) { return DataType.factory(databaseObject); }

    @Override
    public String to(DataType userObject) {
        return Objects.isNull(userObject) ? All.NUMBER.value() : userObject.value();
    }

    @Override
    public Class<String> fromType() { return String.class; }

    @Override
    public Class<DataType> toType() { return DataType.class; }

}
