package com.nubeiot.edge.connector.dashboard;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.sql.SqlContext;
import com.nubeiot.core.sql.SqlProvider;
import com.nubeiot.edge.connector.dashboard.model.DefaultCatalog;

import lombok.Getter;

public class EdgeDashboardServerVerticle extends ContainerVerticle {

    static final String SHARED_EDGE_CONNECTION = "EDGE_CONNECTION";
    @Getter
    private EdgeDashboardEntityHandler entityHandler;

    @Override
    public void start() {
        super.start();
        HttpServerRouter router = new HttpServerRouter().registerApi(InfoRestController.class);
        EdgeDashboardConnectionConfig edgeDashboardConnectionConfig = IConfig.from(nubeConfig.getAppConfig(),
                                                                                   EdgeDashboardConnectionConfig.class);

        this.addSharedData(SHARED_EDGE_CONNECTION, edgeDashboardConnectionConfig.toJson());

        this.addProvider(new HttpServerProvider(router))
            .addProvider(new MicroserviceProvider())
            .addProvider(new SqlProvider<>(DefaultCatalog.DEFAULT_CATALOG, EdgeDashboardEntityHandler.class),
                         this::handler);
    }

    private void handler(SqlContext component) {
        this.entityHandler = (EdgeDashboardEntityHandler) component.getEntityHandler();
    }

}
