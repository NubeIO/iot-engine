package com.nubeiot.core.http.ws;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import io.vertx.ext.bridge.BridgeEventType;

public final class BridgeEventTypeSerialize extends StdSerializer<BridgeEventType> {

    public BridgeEventTypeSerialize() {
        super(BridgeEventType.class);
    }

    @Override
    public void serialize(BridgeEventType value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(BridgeEventType.RECEIVE == value ? "rec" : value.name().toLowerCase());
    }

}
