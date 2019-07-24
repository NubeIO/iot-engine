package com.nubeiot.edge.connector.dashboard;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.micro.MicroserviceProvider;

public class EdgeDashboardServerVerticle extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        HttpServerRouter router = new HttpServerRouter().registerApi(InfoRestController.class);
        this.addProvider(new HttpServerProvider(router)).addProvider(new MicroserviceProvider());
    }

}
