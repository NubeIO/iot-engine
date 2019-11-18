package com.nubeiot.core.cluster;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.NodeListener;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ClusterNodeListener implements NodeListener {

    private static final Logger logger = LoggerFactory.getLogger(ClusterNodeListener.class);
    private final IClusterDelegate clusterDelegate;
    private final EventbusClient controller;
    private final String listenerAddress;

    @Override
    public void nodeAdded(String nodeID) {
        logger.info("Add node: {}", nodeID);
        ClusterNode clusterNode = clusterDelegate.lookupNodeById(nodeID);
        controller.publish(listenerAddress, EventMessage.initial(EventAction.CREATE, clusterNode.toRequestData()));
    }

    @Override
    public void nodeLeft(String nodeID) {
        logger.info("Remove node: {}", nodeID);
        ClusterNode clusterNode = ClusterNode.builder().id(nodeID).build();
        controller.publish(listenerAddress, EventMessage.initial(EventAction.REMOVE, clusterNode.toRequestData()));
    }

}
