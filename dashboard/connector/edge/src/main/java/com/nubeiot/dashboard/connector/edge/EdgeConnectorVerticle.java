package com.nubeiot.dashboard.connector.edge;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;

public final class EdgeConnectorVerticle extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        logger.info("Dashboard Edge connector configuration: {}", this.nubeConfig.getAppConfig().toJson());
        this.addProvider(new HttpServerProvider(initHttpRouter()));
    }

    @SuppressWarnings("unchecked")
    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerApi(EdgeRestController.class).registerEventBusApi(EdgeRestEventApi.class);
    }

}
