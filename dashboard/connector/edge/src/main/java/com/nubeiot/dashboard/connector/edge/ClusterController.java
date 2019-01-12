package com.nubeiot.dashboard.connector.edge;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.cluster.ClusterNode;
import com.nubeiot.core.cluster.ClusterRegistry;
import com.nubeiot.core.cluster.ClusterType;
import com.nubeiot.core.cluster.IClusterDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClusterController implements EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

    @NonNull
    private final Function<String, Object> sharedDataFunc;

    @Override
    public List<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST);
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = List.class)
    public List<JsonObject> list(RequestData data) {
        ClusterType clusterType = (ClusterType) sharedDataFunc.apply(EdgeConnectorVerticle.SHARED_CLUSTER_TYPE);
        IClusterDelegate clusterDelegate = ClusterRegistry.instance().getClusterDelegate(clusterType);
        return clusterDelegate.getAllNodes().stream().map(ClusterNode::toJson).collect(Collectors.toList());
    }

}
