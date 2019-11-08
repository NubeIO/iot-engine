package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;
import java.time.ZoneOffset;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.constructed.DateTime;

public final class DateTimeSerializer extends EncodableSerializer<DateTime> {

    DateTimeSerializer() {
        super(DateTime.class);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        final Object v = value.isFullySpecified()
                         ? value.getGC().toInstant().atOffset(ZoneOffset.UTC)
                         : value.toString();
        gen.writeObject(v);
    }

}
