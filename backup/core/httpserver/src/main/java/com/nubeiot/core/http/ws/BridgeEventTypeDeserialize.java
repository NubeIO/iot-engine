package com.nubeiot.core.http.ws;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import io.vertx.ext.bridge.BridgeEventType;

public final class BridgeEventTypeDeserialize extends StdDeserializer<BridgeEventType> {

    public BridgeEventTypeDeserialize() {
        super(BridgeEventType.class);
    }

    @Override
    public BridgeEventType deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final String jsonValue = p.getText();
        if ("rec".equalsIgnoreCase(jsonValue)) {
            return BridgeEventType.RECEIVE;
        }
        return Arrays.stream(BridgeEventType.values())
                     .filter(t -> t.name().equalsIgnoreCase(jsonValue))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown Bridge Event Type"));
    }

}
