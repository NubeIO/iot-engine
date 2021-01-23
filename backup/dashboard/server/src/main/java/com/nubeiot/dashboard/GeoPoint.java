package com.nubeiot.dashboard;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class GeoPoint {
    private Double lng;
    private Double lat;

    public GeoPoint(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public Double getLongitude() {
        return lng;
    }

    public Double getLatitude() {
        return lat;
    }

    public JsonObject toJsonObject() {
        return new JsonObject().put("type", "Point").put("coordinates", new JsonArray().add(this.lng).add(this.lat));
    }
}
