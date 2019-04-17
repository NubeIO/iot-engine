package com.nubeiot.edge.module.gateway;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.micro.MicroserviceProvider;

public class EdgeGatewayVerticle extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        HttpServerRouter router = new HttpServerRouter();
        this.addProvider(new HttpServerProvider(router))
            .addProvider(new MicroserviceProvider());
    }

}
