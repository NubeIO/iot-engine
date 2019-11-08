package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.bacnet4j.type.constructed.Choice;

public final class ChoiceSerializer extends EncodableSerializer<Choice> {

    ChoiceSerializer() {
        super(Choice.class);
    }

    @Override
    public void serialize(Choice value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        final Object datum = value.getChoiceOptions()
                                  .getPrimitives()
                                  .stream()
                                  .filter(value::isa)
                                  .findFirst()
                                  .map(clazz -> value.getDatum())
                                  .orElse(null);
        if (Objects.nonNull(datum)) {
            gen.writeObject(datum);
        }
    }

}
