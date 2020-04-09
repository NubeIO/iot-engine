package com.nubeiot.edge.installer.model.converter;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.edge.installer.model.type.ModuleType;

public final class ModuleTypeConverter implements Converter<String, ModuleType> {

    @Override
    public ModuleType from(String databaseObject) {
        return ModuleType.factory(databaseObject);
    }

    @Override
    public String to(ModuleType userObject) {
        return Objects.isNull(userObject) ? null : userObject.type();
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
