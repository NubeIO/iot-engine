package com.nubeiot.dashboard.connector.edge;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.HttpServer;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;

public final class EdgeConnectorVerticle extends ContainerVerticle {

    static final EventModel CLUSTER_INFO = EventModel.builder()
                                                     .address("edge.cluster.node")
                                                     .pattern(EventPattern.REQUEST_RESPONSE)
                                                     .event(EventAction.GET_LIST)
                                                     .local(true)
                                                     .build();
    static final String SHARED_CLUSTER_TYPE = "CLUSTER_TYPE";

    @Override
    public void start() {
        super.start();
        logger.info("Dashboard Edge connector configuration: {}", this.nubeConfig.getAppConfig().toJson());
        this.addProvider(new HttpServerProvider(initHttpRouter()), this::registerEventbus);
        this.addSharedData(SHARED_CLUSTER_TYPE, this.nubeConfig.getSystemConfig().getClusterConfig().getType());
    }

    private void registerEventbus(HttpServer httpServer) {
        new EventController(vertx).consume(CLUSTER_INFO, new ClusterController(httpServer::getSharedData));
    }

    @SuppressWarnings("unchecked")
    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerApi(EdgeRestController.class).registerEventBusApi(EdgeRestEventApi.class);
    }

}
