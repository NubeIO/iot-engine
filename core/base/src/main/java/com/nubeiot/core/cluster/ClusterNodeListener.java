package com.nubeiot.core.cluster;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.reactivex.core.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClusterNodeListener implements NodeListener {

    private static final Logger logger = LoggerFactory.getLogger(ClusterNodeListener.class);
    private final EventBus eventBus;
    private final String address;

    @Override
    public void nodeAdded(String nodeID) {
        logger.info("Add node: {}", nodeID);
        eventBus.send(address, nodeID);
    }

    @Override
    public void nodeLeft(String nodeID) {
        logger.info("Remove node: {}", nodeID);
        eventBus.send(address, nodeID);
    }

}
