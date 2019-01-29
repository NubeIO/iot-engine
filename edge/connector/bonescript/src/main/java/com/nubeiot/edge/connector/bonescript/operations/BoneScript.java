package com.nubeiot.edge.connector.bonescript.operations;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.FEATURES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.POINTS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PROPERTIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING;

import com.nubeiot.edge.connector.bonescript.SingletonBBPinMapping;
import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import lombok.NonNull;

public class BoneScript {

    public static void init(@NonNull Vertx vertx, @NonNull JsonObject db) {
        attachInterrupts(db);
    }

    private static void attachInterrupts(JsonObject db) {
        JsonObject points = db.getJsonObject(THING)
                              .getJsonObject(FEATURES)
                              .getJsonObject(POINTS)
                              .getJsonObject(PROPERTIES);
        points.forEach(point -> attachInterrupts(db, point.getKey()));
    }

    private static void attachInterrupts(JsonObject db, String id) {
        BBPinMapping bbPinMapping = SingletonBBPinMapping.getInstance();
        if (bbPinMapping.getDigitalInPins().contains(id)) {
            // TODO: on change, BeagleBone inputs do operation
        }
    }

}
