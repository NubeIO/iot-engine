package com.nubeiot.dashboard.connector.edge;

import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;

public final class EdgeConnectorVerticle extends ContainerVerticle {

    static final EventModel CLUSTER_INFO = EventModel.builder()
                                                     .address("edge.cluster.node")
                                                     .pattern(EventPattern.REQUEST_RESPONSE)
                                                     .event(EventAction.GET_LIST)
                                                     .local(true)
                                                     .build();

    @Override
    public void start() {
        super.start();
        logger.info("Dashboard Edge connector configuration: {}", this.nubeConfig.getAppConfig().toJson());
        this.addProvider(new HttpServerProvider(initHttpRouter()));
    }

    @Override
    public void registerEventbus(EventbusClient eventClient) {
        ClusterType clusterType = this.nubeConfig.getSystemConfig().getClusterConfig().getType();
        eventClient.register(CLUSTER_INFO, new ClusterController(clusterType));
    }

    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerApi(EdgeRestController.class).registerEventBusApi(EdgeRestEventApi.class);
    }

}
