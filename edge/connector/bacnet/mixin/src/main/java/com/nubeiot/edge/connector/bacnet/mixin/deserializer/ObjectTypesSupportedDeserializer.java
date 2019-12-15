package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Strings;
import com.serotonin.bacnet4j.type.constructed.ObjectTypesSupported;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;

import lombok.NonNull;

public final class ObjectTypesSupportedDeserializer extends BitStringDeserializer<ObjectTypesSupported> {

    ObjectTypesSupportedDeserializer() {
        super(ObjectTypesSupported.class);
    }

    @Override
    public ObjectTypesSupported parse(@NonNull JsonObject values) {
        if (values.isEmpty()) {
            return null;
        }
        ObjectTypesSupported output = new ObjectTypesSupported();
        values.stream()
              .forEach(entry -> output.set(ObjectType.forName(entry.getKey()),
                                           Boolean.parseBoolean(Strings.toString(entry.getValue()))));
        return output;
    }

}
