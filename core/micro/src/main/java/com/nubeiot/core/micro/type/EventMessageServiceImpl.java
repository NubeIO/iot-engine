package com.nubeiot.core.micro.type;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.AbstractServiceReference;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public class EventMessageServiceImpl implements EventMessageService {

    @Override
    public ServiceReference get(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject configuration) {
        return new EventMessageServiceReference(vertx, discovery, record, configuration);
    }

    static class EventMessageServiceReference extends AbstractServiceReference<EventMessagePusher> {

        private final DeliveryOptions config;
        private final EventController controller;

        EventMessageServiceReference(@NonNull Vertx vertx, @NonNull ServiceDiscovery discovery, @NonNull Record record,
                                     @NonNull JsonObject config) {
            super(vertx, discovery, record);
            String sharedKey = config.getString(SHARED_KEY_CONFIG);
            this.controller = Strings.isBlank(sharedKey)
                              ? new EventController(vertx)
                              : SharedDataDelegate.getLocalDataValue(vertx, sharedKey,
                                                                     SharedDataDelegate.SHARED_EVENTBUS);
            this.config = new DeliveryOptions(config.getJsonObject(DELIVERY_OPTIONS_CONFIG, new JsonObject()));
        }

        @Override
        protected EventMessagePusher retrieve() {
            return new Pusher(controller, JsonData.from(this.record().getMetadata().getJsonObject(EVENT_METHOD_CONFIG),
                                                        EventMethodDefinition.class), config,
                              record().getLocation().getString(Record.ENDPOINT), EventPattern.REQUEST_RESPONSE);
        }

    }

}
