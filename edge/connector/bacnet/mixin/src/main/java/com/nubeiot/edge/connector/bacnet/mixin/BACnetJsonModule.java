package com.nubeiot.edge.connector.bacnet.mixin;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin.PropertyValuesSerializer;
import com.nubeiot.edge.connector.bacnet.mixin.serializer.EncodableSerializer;
import com.nubeiot.edge.connector.bacnet.mixin.serializer.ObjectIdentifierKeySerializer;
import com.nubeiot.edge.connector.bacnet.mixin.serializer.PropertyIdentifierKeySerializer;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

final class BACnetJsonModule {

    static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule();
        MODULE.addKeySerializer(ObjectIdentifier.class, new ObjectIdentifierKeySerializer());
        MODULE.addKeySerializer(PropertyIdentifier.class, new PropertyIdentifierKeySerializer());
        ReflectionClass.stream(BACnetJsonModule.class.getPackage().getName(), EncodableSerializer.class,
                               ReflectionClass.publicClass())
                       .map(ReflectionClass::createObject)
                       .forEach(MODULE::addSerializer);
        MODULE.addSerializer(Encodable.class, EncodableSerializer.DEFAULT);
        MODULE.addSerializer(PropertyValuesMixin.class, new PropertyValuesSerializer());
    }
}
