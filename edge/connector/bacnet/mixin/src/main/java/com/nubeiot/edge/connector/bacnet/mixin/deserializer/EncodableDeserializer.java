package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import java.util.Objects;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.PropertyTypeDefinition;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

import lombok.NonNull;

public interface EncodableDeserializer<T extends Encodable, V> {

    /**
     * Parse value based on property identifier
     *
     * @param propertyIdentifier Given property identifier
     * @param value              Given value
     * @return BACnet Encodable value
     * @throws NubeException if catching any error when parsing
     */
    static Encodable parse(@NonNull PropertyIdentifier propertyIdentifier, Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        final PropertyTypeDefinition definition = ObjectProperties.getPropertyTypeDefinition(propertyIdentifier);
        if (Objects.isNull(definition)) {
            return null;
        }
        final EncodableDeserializer deserializer = EncodableDeserializerRegistry.lookup(definition.getClazz());
        if (definition.isCollection()) {
            return parse(value, definition, new SequenceOfDeserializer(definition, deserializer));
        }
        return parse(value, definition, deserializer);
    }

    @SuppressWarnings("unchecked")
    static Encodable parse(@NonNull Object value, @NonNull PropertyTypeDefinition definition,
                           @NonNull EncodableDeserializer deserializer) {
        return Functions.getOrThrow(() -> deserializer.parse(deserializer.cast(value)), t -> {
            final String msg = Strings.format("Cannot parse {0} '{1}' of '{2}' as data type {3}",
                                              definition.isCollection() ? "list item" : "value", value,
                                              definition.getPropertyIdentifier(), definition.getClazz().getName());
            return NubeExceptionConverter.friendly(t, msg);
        });
    }

    /**
     * Same with {@link #parse(PropertyIdentifier, Object)} but with lenient that means returns {@code null} if {@code
     * value} is non-parsable
     *
     * @param propertyIdentifier Given property identifier
     * @param value              Given value
     * @return BACnet Encodable value
     */
    static Encodable parseLenient(@NonNull PropertyIdentifier propertyIdentifier, Object value) {
        return Functions.getIfThrow(() -> parse(propertyIdentifier, value)).orElse(null);
    }

    @NonNull Class<T> encodableClass();

    @NonNull Class<V> fromClass();

    @NonNull
    default V cast(@NonNull Object value) {
        return fromClass().cast(value);
    }

    T parse(@NonNull V value);

}
