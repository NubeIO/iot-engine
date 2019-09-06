package com.nubeiot.dashboard.connector.edge;

import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;
import com.nubeiot.eventbus.edge.EdgeMonitorEventBus;

import lombok.Getter;

@Getter
final class EdgeRestEventApi extends AbstractRestEventApi {

    @Override
    public EdgeRestEventApi initRouter() {
        addRouter(EdgeInstallerEventBus.BIOS_STATUS, "/status", "/:id");
        addRouter(EdgeInstallerEventBus.BIOS_TRANSACTION, "/modules/transactions", "/:transaction_id");
        addRouter(EdgeInstallerEventBus.BIOS_INSTALLER, "/modules", "/:service_id");
        addRouter(EdgeInstallerEventBus.getServiceInstaller(false), "/services", "/:service_id");
        addRouter(EdgeInstallerEventBus.getServiceTransaction(false), "/services/transactions", "/:transaction_id");
        addRouter(EdgeInstallerEventBus.getServiceLastTransaction(false), "/services/:module_id/transactions",
                  "/:transaction_id");
        addRouter(EdgeMonitorEventBus.getMonitorStatus(false), "/monitor/status", "/:id");
        addRouter(EdgeMonitorEventBus.getMonitorNetworkStatus(false), "/monitor/network/status", "/:id");
        addRouter(EdgeConnectorVerticle.CLUSTER_INFO, "/cluster/nodes", "/:node_id");
        return this;
    }

}
