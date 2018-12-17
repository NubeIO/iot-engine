package com.nubeiot.dashboard.connector.edge;

import java.util.Map;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.http.AbstractEventBusRestApi;
import com.nubeiot.core.http.EventBusRestApi;

import io.vertx.core.http.HttpMethod;
import lombok.Getter;

@Getter
final class EdgeEventBusRestApi extends AbstractEventBusRestApi {

    @Override
    protected Map<EventType, HttpMethod> initHttpEventMap() {
        Map<EventType, HttpMethod> map = EventBusRestApi.defaultEventHttpMap();
        map.put(EventType.HALT, HttpMethod.PATCH);
        return map;
    }

    @Override
    protected void initRoute() {
        addRouter(EventModel.EDGE_BIOS_TRANSACTION, "/module/transaction", "transaction_id");
        addRouter(EventModel.EDGE_BIOS_INSTALLER, "/module", "service_id");
        addRouter(EventModel.EDGE_BIOS_STATUS, "/status", "id");
        addRouter(EventModel.EDGE_APP_INSTALLER, "/service", "service_id");
        addRouter(EventModel.EDGE_APP_TRANSACTION, "/service/transaction", "transaction_id");
    }

}
