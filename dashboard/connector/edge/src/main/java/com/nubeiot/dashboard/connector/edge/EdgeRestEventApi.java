package com.nubeiot.dashboard.connector.edge;

import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

import lombok.Getter;

@Getter
final class EdgeRestEventApi extends AbstractRestEventApi {

    @Override
    protected void initRoute() {
        addRouter(EdgeInstallerEventBus.BIOS_STATUS, "/status", "/status/:id");
        addRouter(EdgeInstallerEventBus.BIOS_TRANSACTION, "/modules/transactions",
                  "/modules/transactions/:transaction_id");
        addRouter(EdgeInstallerEventBus.BIOS_INSTALLER, "/modules", "/modules/:service_id");
        addRouter(EdgeInstallerEventBus.SERVICE_INSTALLER, "/services", "/services/:service_id");
        addRouter(EdgeInstallerEventBus.SERVICE_TRANSACTION, "/services/transactions",
                  "/services/transactions/:transaction_id");
        addRouter(EdgeConnectorVerticle.CLUSTER_INFO, "/cluster/nodes", "/cluster/nodes/:node_id");
    }

}
