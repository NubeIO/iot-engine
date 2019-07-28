package com.nubeiot.edge.connector.dashboard;

import com.nubeiot.core.http.rest.AbstractRestEventApi;

import lombok.Getter;

@Getter
final class EdgeDashboardRestEventApi extends AbstractRestEventApi {

    @Override
    protected void initRoute() {
        addRouter(EdgeDashboardEventModel.EDGE_DASHBOARD_CONNECTION, "/edge-dashboard/connection", "/:id");
    }

}
