package com.nubeiot.edge.connector.bacnet.simulator;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.utils.Configs;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;

public class BACnetSimulator extends BACnetVerticle {

    @Override
    protected void initLocalPoints(String localPointsAddress, ServiceDiscoveryController localController) {
        JsonObject points = Configs.loadJsonConfig("points.json");
        bacnetInstances.forEach((s, baCnet) -> baCnet.initialiseLocalObjectsFromJson(points));
    }

}
