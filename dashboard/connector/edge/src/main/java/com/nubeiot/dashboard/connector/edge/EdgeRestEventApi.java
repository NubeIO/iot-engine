package com.nubeiot.dashboard.connector.edge;

import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

import lombok.Getter;

@Getter
final class EdgeRestEventApi extends AbstractRestEventApi {

    @Override
    protected void initRoute() {
        addRouter(EdgeInstallerEventBus.BIOS_STATUS, "/status", "/:id");
        addRouter(EdgeInstallerEventBus.BIOS_TRANSACTION, "/modules/transactions", "/:transaction_id");
        addRouter(EdgeInstallerEventBus.BIOS_INSTALLER, "/modules", "/:service_id");
        addRouter(EdgeInstallerEventBus.getServiceInstaller(false), "/services", "/:service_id");
        addRouter(EdgeInstallerEventBus.getServiceTransaction(false), "/services/transactions", "/:transaction_id");
        addRouter(EdgeConnectorVerticle.CLUSTER_INFO, "/cluster/nodes", "/:node_id");
    }

}
