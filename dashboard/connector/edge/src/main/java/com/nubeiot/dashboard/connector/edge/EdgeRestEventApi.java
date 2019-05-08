package com.nubeiot.dashboard.connector.edge;

import com.nubeiot.core.http.rest.AbstractRestEventApi;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.Getter;

@Getter
final class EdgeRestEventApi extends AbstractRestEventApi {

    @Override
    protected void initRoute() {
        addRouter(InstallerEventModel.BIOS_STATUS, "/status", "/:id");
        addRouter(InstallerEventModel.BIOS_TRANSACTION, "/modules/transactions", "/:transaction_id");
        addRouter(InstallerEventModel.BIOS_INSTALLER, "/modules", "/:service_id");
        addRouter(InstallerEventModel.getServiceInstaller(false), "/services", "/:service_id");
        addRouter(InstallerEventModel.getServiceTransaction(false), "/services/transactions", "/:transaction_id");
        addRouter(EdgeConnectorVerticle.CLUSTER_INFO, "/cluster/nodes", "/:node_id");
    }

}
