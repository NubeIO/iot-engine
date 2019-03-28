package com.nubeiot.edge.connector.driverapi;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.micro.MicroserviceProvider;

public class DriverVerticle extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        logger.info("DriverAPI configuration: {}", this.nubeConfig.getAppConfig().toJson());

        this.addProvider(new HttpServerProvider(new HttpServerRouter())).addProvider(new MicroserviceProvider());
    }

    public String configFile() { return "driverapi.json"; }


}
