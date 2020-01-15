package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import com.serotonin.bacnet4j.type.constructed.PriorityValue;

import lombok.NonNull;

final class PriorityValueDeserializer implements EncodableDeserializer<PriorityValue, Object> {

    @Override
    public @NonNull Class<PriorityValue> encodableClass() {
        return PriorityValue.class;
    }

    @Override
    public @NonNull Class<Object> javaClass() {
        return Object.class;
    }

    @Override
    public PriorityValue parse(@NonNull Object value) {
        //TODO implement it
        return null;
    }

}
