package com.nubeiot.edge.connector.bacnet.simulator;

import java.net.URL;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;

public class BACnetSimulator extends BACnetVerticle {

    @Override
    protected void getLocalPoints() {
        final URL POINTS_RESOURCE = FileUtils.class.getClassLoader().getResource("points.json");
        JsonObject points = new JsonObject(FileUtils.readFileToString(POINTS_RESOURCE.toString()));
        bacnetInstance.initialiseLocalObjectsFromJson(points);
    }

}
