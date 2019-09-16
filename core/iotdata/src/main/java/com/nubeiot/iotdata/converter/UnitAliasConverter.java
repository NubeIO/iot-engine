package com.nubeiot.iotdata.converter;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.iotdata.unit.UnitAlias;

public class UnitAliasConverter implements Converter<String, UnitAlias> {

    @Override
    public UnitAlias from(String databaseObject) {
        return Strings.isBlank(databaseObject) ? null : JsonData.from(databaseObject, UnitAlias.class);
    }

    @Override
    public String to(UnitAlias userObject) {
        return Objects.isNull(userObject) ? null : userObject.toJson().encode();
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    public Class<UnitAlias> toType() {
        return UnitAlias.class;
    }

}
