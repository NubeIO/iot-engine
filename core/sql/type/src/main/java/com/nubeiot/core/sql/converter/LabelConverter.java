package com.nubeiot.core.sql.converter;

import java.util.Objects;

import org.jooq.Converter;

import io.github.zero88.utils.Strings;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.type.Label;

public final class LabelConverter implements Converter<String, Label> {

    @Override
    public Label from(String databaseObject) {
        return Strings.isBlank(databaseObject) ? null : JsonData.from(databaseObject, Label.class);
    }

    @Override
    public String to(Label userObject) {
        return Objects.isNull(userObject) ? null : userObject.toJson().encode();
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    public Class<Label> toType() {
        return Label.class;
    }

}
