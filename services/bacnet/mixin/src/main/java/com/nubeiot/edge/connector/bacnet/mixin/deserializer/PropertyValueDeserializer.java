package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import io.vertx.core.json.JsonObject;

import com.serotonin.bacnet4j.type.constructed.PriorityValue;

import lombok.NonNull;

public class PropertyValueDeserializer implements EncodableDeserializer<PriorityValue, JsonObject> {

    @Override
    public @NonNull Class<PriorityValue> encodableClass() {
        return PriorityValue.class;
    }

    @Override
    public @NonNull Class<JsonObject> javaClass() {
        return JsonObject.class;
    }

    @Override
    public PriorityValue parse(@NonNull JsonObject value) {
        //TODO implement it
        return null;
    }

}
