package com.nubeiot.edge.core.model.converter;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.edge.core.loader.ModuleType;

public final class ModuleTypeConverter implements Converter<String, ModuleType> {

    @Override
    public ModuleType from(String databaseObject) {
        return ModuleType.factory(databaseObject);
    }

    @Override
    public String to(ModuleType userObject) {
        return Objects.isNull(userObject) ? null : userObject.name();
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    public Class<ModuleType> toType() {
        return ModuleType.class;
    }

}
