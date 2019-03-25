package com.nubeiot.core.http.dynamic.mock;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.micro.MicroserviceProvider;

public class GatewayServer extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        this.addProvider(new HttpServerProvider(new HttpServerRouter())).addProvider(new MicroserviceProvider());
    }

    public String configFile() { return "gateway.json"; }

}
