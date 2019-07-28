package com.nubeiot.edge.connector.dashboard;

import java.util.HashMap;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.connector.dashboard.model.DefaultCatalog;

public class EdgeDashboardServerVerticle extends ContainerVerticle {

    static final String SHARED_EDGE_CONNECTION = "EDGE_CONNECTION";
    private final EdgeDashboardEventHandler edgeDashboardEventHandler = new EdgeDashboardEventHandler();

    @Override
    public void start() {
        super.start();
        HttpServerRouter router = new HttpServerRouter().registerApi(InfoRestController.class);
        EdgeDashboardConnectionConfig edgeDashboardConnectionConfig = IConfig.from(nubeConfig.getAppConfig(),
                                                                                   EdgeDashboardConnectionConfig.class);

        this.addSharedData(SHARED_EDGE_CONNECTION, edgeDashboardConnectionConfig.toJson());

        this.addProvider(new HttpServerProvider(router)).addProvider(new MicroserviceProvider(), this::publishServices)
            .addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, EdgeDashboardEntityHandler.class),
                         this::handler);
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(EdgeDashboardEventModel.EDGE_DASHBOARD_CONNECTION, edgeDashboardEventHandler);
    }

    private void publishServices(MicroContext microContext) {
        microContext.getLocalController()
                    .addEventMessageRecord("edge-dashboard-connection",
                                           EdgeDashboardEventModel.EDGE_DASHBOARD_CONNECTION.getAddress(),
                                           EventMethodDefinition.create("/edge-dashboard/connection",
                                                                        edgeDashboardConnectionActionMethodMapping()))
                    .subscribe();
    }

    private ActionMethodMapping edgeDashboardConnectionActionMethodMapping() {
        return () -> new HashMap<EventAction, HttpMethod>() {
            {
                put(EventAction.GET_ONE, HttpMethod.GET);
                put(EventAction.PATCH, HttpMethod.PATCH);
            }
        };
    }

    private void handler(SqlContext component) {
        edgeDashboardEventHandler.setEntityHandler((EdgeDashboardEntityHandler) component.getEntityHandler());
    }

}
