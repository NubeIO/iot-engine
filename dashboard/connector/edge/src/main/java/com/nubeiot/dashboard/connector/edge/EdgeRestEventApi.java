package com.nubeiot.dashboard.connector.edge;

import java.util.Map;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.core.http.rest.RestEventApi;
import com.nubeiot.eventbus.edge.EdgeEventBus;

import io.vertx.core.http.HttpMethod;
import lombok.Getter;

@Getter
final class EdgeRestEventApi extends AbstractRestEventApi {

    @Override
    protected void initRoute() {
        addRouter(EdgeEventBus.BIOS_TRANSACTION, "/module/transaction", "transaction_id");
        addRouter(EdgeEventBus.BIOS_INSTALLER, "/module", "service_id");
        addRouter(EdgeEventBus.BIOS_STATUS, "/status", "id");
        addRouter(EdgeEventBus.APP_INSTALLER, "/service", "service_id");
        addRouter(EdgeEventBus.APP_TRANSACTION, "/service/transaction", "transaction_id");
        addRouter(EdgeConnectorVerticle.CLUSTER_INFO, "/cluster/node", "node_id");
    }

}
