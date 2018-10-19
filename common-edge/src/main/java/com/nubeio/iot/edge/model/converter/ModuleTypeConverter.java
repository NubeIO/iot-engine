package com.nubeio.iot.edge.model.converter;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeio.iot.edge.loader.ModuleType;
import com.nubeio.iot.edge.loader.ModuleTypeFactory;

public final class ModuleTypeConverter implements Converter<String, ModuleType> {

    @Override
    public ModuleType from(String databaseObject) {
        return ModuleTypeFactory.factory(databaseObject);
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
