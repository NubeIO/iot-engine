package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nubeiot.core.utils.DateTimes;
import com.serotonin.bacnet4j.type.primitive.Date;

public final class DateSerializer extends EncodableSerializer<Date> {

    DateSerializer() {
        super(Date.class);
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Object v = value.isSpecific() ? DateTimes.formatDate(
            DateTimes.toUTC(value.calculateGC().toInstant()).toOffsetDateTime()) : value.toString();
        gen.writeObject(v);
    }

}
