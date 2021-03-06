package com.nubeiot.edge.connector.bacnet.mixin;

import com.serotonin.bacnet4j.type.Encodable;

/**
 * Represents an {@code Encodable} that is able serialize to non-primitive java object, such as: JsonObject, JsonArray,
 * Collections, etc
 *
 * @param <T> Type of encodable
 */
public interface EncodableMixin<T extends Encodable> extends BACnetMixin {

    T unwrap();

}
