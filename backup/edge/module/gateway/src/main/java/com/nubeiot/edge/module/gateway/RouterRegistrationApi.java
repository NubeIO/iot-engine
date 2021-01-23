package com.nubeiot.edge.module.gateway;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.eventbus.edge.gateway.GatewayEventBus;

public final class RouterRegistrationApi extends AbstractRestEventApi {

    private static Map<EventAction, HttpMethod> map() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.CREATE, HttpMethod.POST);
        map.put(EventAction.REMOVE, HttpMethod.DELETE);
        return map;
    }

    @Override
    public RouterRegistrationApi initRouter() {
        addRouter(GatewayEventBus.ROUTER_REGISTRATION, "/register", "/:registration");
        return this;
    }

    @Override
    protected ActionMethodMapping initHttpEventMap() {
        return ActionMethodMapping.create(map());
    }

}
