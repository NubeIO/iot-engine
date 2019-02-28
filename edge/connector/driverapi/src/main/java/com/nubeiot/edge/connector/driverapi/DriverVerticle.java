package com.nubeiot.edge.connector.driverapi;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;

public class DriverVerticle extends ContainerVerticle {

    @Override
    public void start() {
        super.start();

        final NubeConfig nubeConfig = IConfig.from(config(), NubeConfig.class);
        logger.info("DriverAPI configuration: {}", this.nubeConfig.getAppConfig().toJson());

        this.addProvider(new HttpServerProvider(initHttpRouter()));
        this.eventController = registerEventBus(new EventController(vertx));
    }

    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerEventBusApi(DriverRouteDefinitions.class);
    }

    private EventController registerEventBus(EventController controller) {
        controller.register(DriverEventModels.POINTS, new DriverEventHandler(vertx, DriverEventModels.POINTS));
        return controller;
    }

}
