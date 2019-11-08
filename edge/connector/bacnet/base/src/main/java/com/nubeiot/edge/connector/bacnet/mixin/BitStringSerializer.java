package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nubeiot.core.utils.Functions;
import com.serotonin.bacnet4j.type.primitive.BitString;

public final class BitStringSerializer extends EncodableSerializer<BitString> {

    BitStringSerializer() {
        super(BitString.class);
    }

    @Override
    public void serialize(BitString value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeObject(Arrays.stream(value.getClass().getDeclaredMethods())
                              .filter(m -> m.getReturnType() == boolean.class && m.getParameterCount() == 0)
                              .collect(Collectors.toMap(m -> BACnetMixin.standardizeKey(m.getName().substring(2)),
                                                        m -> Functions.getOrDefault(false,
                                                                                    () -> (Boolean) m.invoke(value)))));
    }

}
