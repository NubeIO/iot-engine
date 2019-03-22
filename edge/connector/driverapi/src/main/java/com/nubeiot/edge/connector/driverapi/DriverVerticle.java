package com.nubeiot.edge.connector.driverapi;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.micro.MicroserviceProvider;

public class DriverVerticle extends ContainerVerticle {

    //    private EndpointsMapper endpointsMapper;

    @Override
    public void start() {
        super.start();
        logger.info("DriverAPI configuration: {}", this.nubeConfig.getAppConfig().toJson());

        this.addProvider(new HttpServerProvider(new HttpServerRouter())).addProvider(new MicroserviceProvider());

        //        this.addProvider(new HttpServerProvider(initHttpRouter()));
        //        this.eventController = registerEventBus(new EventController(vertx));
    }

    public String configFile() { return "driverapi.json"; }

    //    private HttpServerRouter initHttpRouter() {
    //        return new HttpServerRouter().registerEventBusApi(ServerRouteDefinitions.class);
    //    }

    //    private EventController registerEventBus(EventController controller) {
    //        controller.register(DriverEventModels.POINTS,
    //                            new ServerEventHandler(vertx, DriverEventModels.POINTS, eventController,
    //                            endpointsMapper));
    //        controller.register(DriverEventModels.ENDPOINTS,
    //                            new DynamicEndpointsHandler(vertx, DriverEventModels.ENDPOINTS, endpointsMapper));
    //
    //        return controller;
    //    }

}
