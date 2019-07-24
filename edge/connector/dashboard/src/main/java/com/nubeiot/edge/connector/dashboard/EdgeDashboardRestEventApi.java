package com.nubeiot.edge.connector.dashboard;

import com.nubeiot.core.http.rest.AbstractRestEventApi;

import lombok.Getter;

@Getter
final class EdgeDashboardRestEventApi extends AbstractRestEventApi {

    @Override
    public EdgeDashboardRestEventApi initRouter() {
        addRouter(EdgeDashboardEventModel.EDGE_DASHBOARD_CONNECTION, "/edge-dashboard/connection", "/:id");
        return this;
    }

}
