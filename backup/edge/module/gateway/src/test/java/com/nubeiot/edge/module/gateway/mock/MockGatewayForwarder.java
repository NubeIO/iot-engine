package com.nubeiot.edge.module.gateway.mock;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.micro.MicroserviceProvider;

public final class MockGatewayForwarder extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        addProvider(new MicroserviceProvider());
    }

    public String configFile() { return "gatewayService.json"; }

}
