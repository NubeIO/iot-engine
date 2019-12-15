package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import com.serotonin.bacnet4j.type.primitive.Double;

import lombok.NonNull;

public final class DoubleDeserializer implements NumberDeserializer<Double, java.lang.Double> {

    @Override
    public @NonNull Class<Double> encodableClass() {
        return Double.class;
    }

    @Override
    public @NonNull Class<java.lang.Double> fromClass() {
        return java.lang.Double.class;
    }

    @Override
    public java.lang.Double cast(@NonNull Object value) {
        return NumberDeserializer.castToDouble(value);
    }

    @Override
    public Double parse(java.lang.@NonNull Double value) {
        return new Double(value);
    }

}
