package com.nubeiot.core.micro.type;

import java.util.Objects;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.spi.ServiceType;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.utils.Strings;

public interface EventMessageService extends ServiceType {

    /**
     * Name of the type.
     */
    String TYPE = "eventbus-message-service";
    String EVENTBUS_KEY = "eventbus.controller";

    static Record createRecord(String name, String address, JsonObject metadata) {
        JsonObject meta = Objects.isNull(metadata) ? new JsonObject() : metadata.copy();
        return new Record().setType(TYPE)
                           .setName(Strings.requireNotBlank(name))
                           .setLocation(new JsonObject().put(Record.ENDPOINT, Strings.requireNotBlank(address)))
                           .setMetadata(meta.put(EVENTBUS_KEY, SharedDataDelegate.SHARED_EVENTBUS));
    }

    @Override
    default String name() {
        return TYPE;
    }

}
