package com.nubeiot.edge.connector.bonescript.operations;

import static com.nubeiot.edge.connector.bonescript.BoneScriptVerticle.BB_DEFAULT_VERSION;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.FEATURES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.POINTS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PROPERTIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING;

import com.nubeiot.edge.connector.bonescript.BoneScriptEntityHandler;
import com.nubeiot.edge.connector.bonescript.MultiThreadDittoDB;
import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;
import com.nubeiot.edge.connector.bonescript.enums.BBVersion;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import lombok.NonNull;

public class BoneScript {

    public static void init(@NonNull Vertx vertx, @NonNull BoneScriptEntityHandler entityHandler,
                            @NonNull MultiThreadDittoDB multiThreadDittoDB, @NonNull JsonObject db) {
        attachInterupts(db);
    }

    private static void attachInterupts(JsonObject db) {
        JsonObject points = db.getJsonObject(THING)
                              .getJsonObject(FEATURES)
                              .getJsonObject(POINTS)
                              .getJsonObject(PROPERTIES);
        points.forEach(point -> attachInterupt(db, point.getKey()));
    }

    private static void attachInterupt(JsonObject db, String id) {
        BBPinMapping bbPinMapping = BBVersion.getBbPinMapping(BB_DEFAULT_VERSION);
        if (bbPinMapping.getDigitalInPins().contains(id)) {
            // TODO: on change, BeagleBone inputs do operation
        }
    }

}
