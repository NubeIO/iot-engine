package com.nubeiot.dashboard.connector.edge;

import java.util.HashMap;
import java.util.Map;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.http.EventBusRestApi;

import io.vertx.core.http.HttpMethod;
import lombok.Getter;

@Getter
final class EdgeEventBusRestApi extends EventBusRestApi {

    EdgeEventBusRestApi() {
        initRoute();
    }

    @Override
    protected Map<EventType, HttpMethod> initHttpEventMapping() {
        Map<EventType, HttpMethod> map = new HashMap<>();
        map.put(EventType.CREATE, HttpMethod.POST);
        map.put(EventType.UPDATE, HttpMethod.PUT);
        map.put(EventType.HALT, HttpMethod.PATCH);
        map.put(EventType.REMOVE, HttpMethod.DELETE);
        map.put(EventType.GET_ONE, HttpMethod.GET);
        map.put(EventType.GET_LIST, HttpMethod.GET);
        return map;
    }

    private void initRoute() {
        addRouter(EventModel.EDGE_BIOS_TRANSACTION, "/module/transaction", "transaction_id");
        addRouter(EventModel.EDGE_BIOS_INSTALLER, "/module", "service_id");
        addRouter(EventModel.EDGE_BIOS_STATUS, "/status", "id");
        addRouter(EventModel.EDGE_APP_INSTALLER, "/service", "service_id");
        addRouter(EventModel.EDGE_APP_TRANSACTION, "/service/transaction", "transaction_id");
    }

}
