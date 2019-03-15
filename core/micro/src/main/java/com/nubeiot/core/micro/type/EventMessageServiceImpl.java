package com.nubeiot.core.micro.type;

import java.util.Objects;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.AbstractServiceReference;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventPattern;

class EventMessageServiceImpl implements EventMessageService {

    @Override
    public ServiceReference get(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject configuration) {
        return null;
    }

    static class EventMessageServiceReference extends AbstractServiceReference<EventMessagePusher> {

        private final DeliveryOptions config;

        public EventMessageServiceReference(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject config) {
            super(vertx, discovery, record);
            this.config = Objects.isNull(config) ? null : new DeliveryOptions(config);
        }

        @Override
        protected EventMessagePusher retrieve() {
            return new Pusher(new EventController(vertx), record().getLocation().getString(Record.ENDPOINT),
                              EventPattern.REQUEST_RESPONSE);
        }

    }

}
