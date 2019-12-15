package com.nubeiot.edge.connector.bacnet.mixin;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.serotonin.bacnet4j.type.Encodable;

final class BACnetJsonModule {

    static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule();
        ReflectionClass.stream(BACnetJsonModule.class.getPackage().getName(), EncodableSerializer.class,
                               ReflectionClass.publicClass())
                       .map(ReflectionClass::createObject)
                       .forEach(MODULE::addSerializer);
        MODULE.addSerializer(Encodable.class, EncodableSerializer.DEFAULT);
    }
}
