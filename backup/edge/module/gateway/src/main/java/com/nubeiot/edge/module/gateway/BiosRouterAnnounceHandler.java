package com.nubeiot.edge.module.gateway;

import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.gateway.DynamicRouterRegister;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.ServiceDiscoveryController;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class BiosRouterAnnounceHandler implements EventListener, DynamicRouterRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(BiosRouterAnnounceHandler.class);

    @Getter
    @NonNull
    private final Vertx vertx;
    @Getter
    @NonNull
    private final String sharedKey;
    @NonNull
    private final MicroContext context;
    @Getter
    @NonNull
    private final Set<EventAction> availableEvents;

    @Override
    public @NonNull Logger logger() {
        return LOGGER;
    }

    @Override
    public ServiceDiscoveryController getController() { return context.getLocalController(); }

    @Override
    public void handle(Message<Object> event) { }

    @EventContractor(action = EventAction.MONITOR, returnType = boolean.class)
    public boolean monitor(RequestData requestData) {
        return register(new Record(requestData.body()));
    }

}
