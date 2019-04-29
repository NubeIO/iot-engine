package com.nubeiot.edge.connector.bacnet.simulator;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Configs;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;

public class BACnetMasterTest extends BACnetVerticle {

    @Override
    protected void initLocalPoints(String localPointsAddress) {
        JsonObject points = Configs.loadJsonConfig("points.json");
        bacnetInstances.forEach((s, bacnet) -> bacnet.initialiseLocalObjectsFromJson(points));
    }

}
