package com.nubeiot.edge.connector.driverapi;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.edge.connector.driverapi.handlers.DynamicEndpointsHandler;
import com.nubeiot.edge.connector.driverapi.handlers.ServerEventHandler;
import com.nubeiot.edge.connector.driverapi.models.DriverEventModels;

public class DriverVerticle extends ContainerVerticle {

    private EndpointsMapper endpointsMapper;

    @Override
    public void start() {
        super.start();

        logger.info("DriverAPI configuration: {}", this.nubeConfig.getAppConfig().toJson());

        this.endpointsMapper = new EndpointsMapper();

        this.addProvider(new HttpServerProvider(initHttpRouter()));
        this.eventController = registerEventBus(new EventController(vertx));
    }

    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerEventBusApi(ServerRouteDefinitions.class);
    }

    private EventController registerEventBus(EventController controller) {
        controller.register(DriverEventModels.POINTS,
                            new ServerEventHandler(vertx, DriverEventModels.POINTS, eventController, endpointsMapper));
        controller.register(DriverEventModels.ENDPOINTS,
                            new DynamicEndpointsHandler(vertx, DriverEventModels.ENDPOINTS, endpointsMapper));

        return controller;
    }

}
