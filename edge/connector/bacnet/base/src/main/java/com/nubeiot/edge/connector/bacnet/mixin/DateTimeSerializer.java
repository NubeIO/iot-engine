package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nubeiot.core.utils.DateTimes;
import com.serotonin.bacnet4j.type.constructed.DateTime;

public final class DateTimeSerializer extends EncodableSerializer<DateTime> {

    DateTimeSerializer() {
        super(DateTime.class);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        final Object v = value.isFullySpecified() ? DateTimes.toUTC(value.getGC().toInstant()) : value.toString();
        gen.writeObject(v);
    }

}
