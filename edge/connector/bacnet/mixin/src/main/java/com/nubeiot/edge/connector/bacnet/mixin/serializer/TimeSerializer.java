package com.nubeiot.edge.connector.bacnet.mixin.serializer;

import java.io.IOException;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.primitive.Time;

public final class TimeSerializer extends EncodableSerializer<Time> {

    TimeSerializer() {
        super(Time.class);
    }

    @Override
    public void serialize(Time value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Object v = value.isFullySpecified() ? of(value) : value.toString();
        gen.writeObject(v);
    }

    private OffsetTime of(Time value) {
        return OffsetTime.of(value.getHour(), value.getMinute(), value.getSecond(), value.getHundredth(),
                             ZoneOffset.UTC);
    }

}
