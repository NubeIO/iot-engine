package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import com.serotonin.bacnet4j.type.constructed.PriorityValue;

import lombok.NonNull;

public final class PriorityValueDeserializer implements EncodableDeserializer<PriorityValue, String> {

    @Override
    public @NonNull Class<PriorityValue> encodableClass() {
        return PriorityValue.class;
    }

    @Override
    public @NonNull Class<String> fromClass() {
        return String.class;
    }

    @Override
    public PriorityValue parse(@NonNull String value) {
        //TODO implement it
        return null;
    }

}
