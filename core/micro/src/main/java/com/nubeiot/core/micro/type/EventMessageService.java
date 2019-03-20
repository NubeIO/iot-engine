package com.nubeiot.core.micro.type;

import java.util.Objects;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.spi.ServiceType;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public interface EventMessageService extends ServiceType {

    /**
     * Name of the type.
     */
    String TYPE = "eventbus-message-service";
    String SHARED_KEY_CONFIG = "sharedKey";
    String EVENT_METHOD_CONFIG = "eventMethods";
    String DELIVERY_OPTIONS_CONFIG = "options";

    static Record createRecord(String name, String address, @NonNull EventMethodDefinition definition,
                               JsonObject metadata) {
        JsonObject meta = Objects.isNull(metadata) ? new JsonObject() : metadata.copy();
        return new Record().setType(TYPE)
                           .setName(Strings.requireNotBlank(name))
                           .setMetadata(meta.put(EVENT_METHOD_CONFIG, definition.toJson()))
                           .setLocation(new JsonObject().put(Record.ENDPOINT, Strings.requireNotBlank(address)));
    }

    @Override
    default String name() { return TYPE; }

}
