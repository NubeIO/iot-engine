package com.nubeiot.edge.connector.bacnet.simulator;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Configs;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;

public class BACnetSimulator extends BACnetVerticle {

    @Override
    public String configFile() {
        return "simulator.json";
    }

    @Override
    protected void getLocalPoints() {
        JsonObject points = Configs.loadJsonConfig("points.json");
        bacnetInstance.initialiseLocalObjectsFromJson(points);
    }

}
