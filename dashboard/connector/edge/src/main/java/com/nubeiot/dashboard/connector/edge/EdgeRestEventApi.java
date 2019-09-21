package com.nubeiot.dashboard.connector.edge;

import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.eventbus.edge.installer.EdgeMonitorEventBus;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.Getter;

@Getter
final class EdgeRestEventApi extends AbstractRestEventApi {

    @Override
    public EdgeRestEventApi initRouter() {
        addRouter(InstallerEventModel.BIOS_STATUS, "/status", "/:id");
        addRouter(InstallerEventModel.BIOS_TRANSACTION, "/modules/transactions", "/:transaction_id");
        addRouter(InstallerEventModel.BIOS_INSTALLER, "/modules", "/:service_id");
        addRouter(InstallerEventModel.getServiceInstaller(false), "/services", "/:service_id");
        addRouter(InstallerEventModel.getServiceTransaction(false), "/services/transactions", "/:transaction_id");
        addRouter(InstallerEventModel.getServiceLastTransaction(false), "/services/:module_id/transactions",
                  "/:transaction_id");
        addRouter(EdgeMonitorEventBus.getMonitorStatus(false), "/monitor/status", "/:id");
        addRouter(EdgeMonitorEventBus.getMonitorNetworkStatus(false), "/monitor/network/status", "/:id");
        addRouter(EdgeConnectorVerticle.CLUSTER_INFO, "/cluster/nodes", "/:node_id");
        return this;
    }

}
