package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import io.github.zero.utils.Reflections.ReflectionClass;

import com.serotonin.bacnet4j.obj.PropertyTypeDefinition;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PriorityArray;
import com.serotonin.bacnet4j.type.primitive.BitString;
import com.serotonin.bacnet4j.type.primitive.Enumerated;

import lombok.NonNull;

public final class EncodableDeserializerRegistry {

    public final static Map<Class<Encodable>, EncodableDeserializer> DESERIALIZERS = create();

    @SuppressWarnings("unchecked")
    static Map<Class<Encodable>, EncodableDeserializer> create() {
        return ReflectionClass.stream(EncodableDeserializer.class.getPackage().getName(), EncodableDeserializer.class,
                                      ReflectionClass.publicClass())
                              .map(ReflectionClass::createObject)
                              .collect(HashMap::new, (m, d) -> m.put(d.encodableClass(), d), Map::putAll);
    }

    @NonNull
    public static EncodableDeserializer lookup(@NonNull PropertyTypeDefinition definition) {
        final Class<? extends Encodable> clazz = definition.getClazz();
        if (clazz == PriorityArray.class) {
            return new PriorityArrayDeserializer(definition);
        }
        return lookup(clazz);
    }

    @NonNull
    public static EncodableDeserializer lookup(@NonNull Class<? extends Encodable> clazz) {
        final EncodableDeserializer deserializer = DESERIALIZERS.get(clazz);
        if (Objects.nonNull(deserializer)) {
            return deserializer;
        }
        return DESERIALIZERS.entrySet()
                            .stream()
                            .filter(entry -> ReflectionClass.assertDataType(clazz, entry.getKey()))
                            .map(Entry::getValue)
                            .findFirst()
                            .orElseGet(() -> fallback(clazz));
    }

    @SuppressWarnings("unchecked")
    public static EncodableDeserializer fallback(@NonNull Class<? extends Encodable> clazz) {
        if (ReflectionClass.assertDataType(clazz, Enumerated.class)) {
            return new EnumeratedDeserializer(clazz);
        }
        if (ReflectionClass.assertDataType(clazz, BitString.class)) {
            return new BitStringDeserializer(clazz);
        }
        return new DefaultEncodableDeserializer(clazz);
    }

}
