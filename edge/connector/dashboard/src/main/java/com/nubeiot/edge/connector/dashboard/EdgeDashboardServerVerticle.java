package com.nubeiot.edge.connector.dashboard;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.connector.dashboard.model.DefaultCatalog;

public class EdgeDashboardServerVerticle extends ContainerVerticle {

    static final String SHARED_EDGE_DASHBOARD_CONNECTION_CONFIG = "EDGE_DASHBOARD_CONNECTION_CONFIG";
    private final EdgeDashboardEventHandler edgeDashboardEventHandler = new EdgeDashboardEventHandler();

    @Override
    public void start() {
        super.start();
        EdgeDashboardConnectionConfig edgeDashboardConnectionConfig = IConfig.from(nubeConfig.getAppConfig(),
                                                                                   EdgeDashboardConnectionConfig.class);
        this.addSharedData(SHARED_EDGE_DASHBOARD_CONNECTION_CONFIG, edgeDashboardConnectionConfig.toJson());
        this.addProvider(new HttpServerProvider(initHttpRouter()))
            .addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, EdgeDashboardEntityHandler.class),
                         this::handler);
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(EdgeDashboardEventModel.EDGE_DASHBOARD_CONNECTION, edgeDashboardEventHandler);
    }

    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerEventBusApi(EdgeDashboardRestEventApi.class);
    }

    private void handler(SqlContext component) {
        edgeDashboardEventHandler.setEntityHandler((EdgeDashboardEntityHandler) component.getEntityHandler());
    }

}
