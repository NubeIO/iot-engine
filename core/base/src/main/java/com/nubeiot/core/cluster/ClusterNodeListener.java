package com.nubeiot.core.cluster;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventType;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.reactivex.core.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ClusterNodeListener implements NodeListener {

    private static final Logger logger = LoggerFactory.getLogger(ClusterNodeListener.class);
    private final IClusterDelegate clusterDelegate;
    private final EventBus eventBus;
    private final String listenerAddress;

    @Override
    public void nodeAdded(String nodeID) {
        logger.info("Add node: {}", nodeID);
        JsonObject node = clusterDelegate.lookupNodeById(nodeID).toJson();
        eventBus.send(listenerAddress,
                      EventMessage.success(EventType.CREATE, RequestData.builder().body(node).build()));
    }

    @Override
    public void nodeLeft(String nodeID) {
        logger.info("Remove node: {}", nodeID);
        JsonObject node = ClusterNode.builder().id(nodeID).build().toJson();
        eventBus.send(listenerAddress,
                      EventMessage.success(EventType.REMOVE, RequestData.builder().body(node).build()));
    }

}
