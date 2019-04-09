package com.nubeiot.edge.module.gateway;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.micro.MicroserviceProvider;

public class EdgeGatewayVerticle extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        HttpServerRouter router = new HttpServerRouter().registerEventBusApi(EdgeGatewayRestEventApi.class);
        this.addProvider(new HttpServerProvider(router))
            .addProvider(new MicroserviceProvider());
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(EdgeGatewayEventModel.infoModel, new EdgeGatewayEventHandler());
    }

}
