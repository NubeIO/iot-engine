package com.nubeiot.edge.module.gateway;

import java.util.Collections;
import java.util.List;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;

import lombok.Getter;

public class EdgeGatewayEventHandler implements EventHandler {

    @Getter
    private List<EventAction> availableEvents = Collections.singletonList(EventAction.GET_LIST);

    @EventContractor(action = EventAction.GET_LIST)
    public JsonObject info(RequestData data) {
        return new JsonObject().put("name", "Edget Gateway Verticle")
            .put("version", "1.0")
            .put("vert.x_version", "3.4.1")
            .put("java_version", "8.0");
    }

}
