package com.nubeiot.edge.connector.bonescript.operations;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO_ENABLE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO_HOST;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO_HTTP_BASIC;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.FEATURES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PROPERTIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.SETTINGS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING_ID;

import com.nubeiot.core.exceptions.InitializerError;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Ditto {

    private static Ditto instance;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private JsonObject db;

    private Boolean dittoEnable = false;
    private String dittoHost = "";
    private String dittoHttpBasic = "";
    private String thingId = "";

    private Ditto(JsonObject db) {
        instance = this;
        this.db = db;

        JsonObject properties = db.getJsonObject(THING)
                                  .getJsonObject(FEATURES)
                                  .getJsonObject(SETTINGS)
                                  .getJsonObject(PROPERTIES);

        updateDittoEnable(properties);
        updateDittoHost(properties);
        updateDittoHttpBasic(properties);
        updateThingId(db);
        logger.info("Updated Ditto Status successfully...");
    }

    public static void init(@NonNull JsonObject dittoData) {
        new Ditto(dittoData);
    }

    public static Ditto getInstance() {
        if (instance != null) {
            return instance;
        }
        throw new InitializerError("Ditto initialization failed...");
    }

    private void updateDittoEnable(JsonObject properties) {
        boolean dittoEnable = properties.getBoolean(DITTO_ENABLE, false);
        this.dittoEnable = dittoEnable;
        logger.info("Ditto Enable is updated to {}", dittoEnable);
    }

    private void updateDittoHost(JsonObject properties) {
        String dittoHost = properties.getString(DITTO_HOST);
        this.dittoHost = dittoHost;
        if (dittoHost != null) {
            this.dittoHost = dittoHost;
            logger.info("Ditto Host is updated to {}", dittoHost);
        }
    }

    private void updateDittoHttpBasic(JsonObject properties) {
        String dittoHttpBasic = properties.getString(DITTO_HTTP_BASIC);
        this.dittoHttpBasic = dittoHttpBasic;
        if (dittoHttpBasic != null) {
            this.dittoHttpBasic = dittoHttpBasic;
            logger.info("Ditto HTTP Basic is updated to {}", dittoHttpBasic);
        }
    }

    private void updateThingId(JsonObject dittoData) {
        String thingId = dittoData.getJsonObject(THING).getString(THING_ID);
        if (thingId != null) {
            this.thingId = thingId;
            logger.info("ThingId is update to {}", thingId);
        }
    }

}
