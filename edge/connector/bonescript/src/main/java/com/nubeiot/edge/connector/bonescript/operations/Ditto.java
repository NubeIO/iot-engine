package com.nubeiot.edge.connector.bonescript.operations;

import static com.nubeiot.core.http.ApiConstants.CONTENT_TYPE;
import static com.nubeiot.core.http.ApiConstants.DEFAULT_CONTENT_TYPE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO_ENABLE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO_HOST;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO_HTTP_BASIC;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.ENABLE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.FEATURES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.LAST_UPDATED;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.LAST_VALUE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRIORITY_ARRAY;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PROPERTIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.SETTINGS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING_ID;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.TOLERANCE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;

import java.time.Duration;
import java.time.Instant;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.core.utils.Strings;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Ditto {

    private static final Logger logger = LoggerFactory.getLogger(Ditto.class);

    private static Ditto instance;
    public final int POST_RATE = 1000;  // Limits posts to ditto to a maximum rate of 1 update per 1000 ms
    private JsonObject db;

    private Boolean dittoEnable = false;
    private String dittoHost = "";
    private String dittoHttpBasic = "";
    private String thingId = "";

    private Ditto(JsonObject db) {
        this.db = db;

        JsonObject properties = db.getJsonObject(THING)
                                  .getJsonObject(FEATURES)
                                  .getJsonObject(SETTINGS)
                                  .getJsonObject(PROPERTIES);

        updateDittoEnable(properties);
        updateDittoHost(properties);
        updateDittoHttpBasic(properties);
        updateThingId(db);
        instance = this;
        logger.info("Updated Ditto Status successfully...");
    }

    public static void pointToDitto(Vertx vertx, JsonObject point, String id, long timestamp) {
        if (instance.dittoEnable && Strings.isNotBlank(instance.dittoHost) && Strings.isNotBlank(instance.thingId)) {
            point.getJsonObject(DITTO).put(LAST_VALUE, point.getValue(VALUE));
            point.getJsonObject(DITTO).put(LAST_UPDATED, timestamp);
            String uri = Strings.format("{0}/api/2/things/{1}/features/points/properties/{2}", instance.dittoHost,
                                        instance.thingId, id);

            HttpClient client = vertx.getDelegate().createHttpClient();
            HttpClientRequest request = client.requestAbs(HttpMethod.PUT, uri, response -> {
                if (response.statusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                    logger.info("Posting on Ditto been made successfully");
                } else {
                    logger.error("Something went wrong on posting Ditto value onto the server");
                }
            });

            request.setChunked(true);
            request.putHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
            if (Strings.isNotBlank(instance.dittoHttpBasic)) {
                request.putHeader("Authorization", instance.dittoHttpBasic);
            }
            request.write(point.encode()).end();
        }
    }

    public static Ditto getInstance(@NonNull JsonObject dittoData) {
        if (instance == null) {
            new Ditto(dittoData);
        }
        return instance;
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
        if (dittoHost != null) {
            this.dittoHost = dittoHost;
            logger.info("Ditto Host is updated to {}", dittoHost);
        }
    }

    private void updateDittoHttpBasic(JsonObject properties) {
        String dittoHttpBasic = properties.getString(DITTO_HTTP_BASIC);
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

    /**
     * Checks if the value or property of a point has changed, and if so returns true to signify that the point in Ditto
     * should be updated. Limits how often true is returned when checking if the value has changed in case the tolerance
     * (if a tolerance is provided) is too low.
     */
    public boolean shouldDittoUpdate(JsonObject point, String prop, Object newValue, JsonObject oldPriorityArray) {
        if (this.dittoEnable && point.containsKey(DITTO) && point.getJsonObject(DITTO).getBoolean(ENABLE)) {
            // Only check if Ditto should update if it's been longer than the postRate since the last update
            if (!point.getJsonObject(DITTO).containsKey(LAST_UPDATED) ||
                isEnoughTimeToUpdateDitto(point.getJsonObject(DITTO))) {

                if (prop.equals(VALUE)) {
                    return shouldDittoUpdateWithNewValue(point, newValue, oldPriorityArray);
                } else {
                    if (!point.getJsonObject(DITTO).containsKey(LAST_UPDATED)) {
                        return true;
                    }
                    return point.getDouble(prop) != newValue;
                }
            }
        }
        return false;
    }

    public boolean shouldDittoUpdate(JsonObject point, String prop, Object newValue) {
        return this.shouldDittoUpdate(point, prop, newValue, null);
    }

    boolean shouldDittoUpdateWithNewValue(JsonObject point, Object newValue, JsonObject oldPriorityArray) {
        JsonObject dittoPoint = point.getJsonObject(DITTO);
        JsonObject priorityArray = point.getJsonObject(PRIORITY_ARRAY);
        // If the point hasn't been updated in Ditto yet, Ditto should update
        if (!dittoPoint.containsKey(LAST_VALUE)) {
            return true;
        }

        // If the point has a tolerance, compare the new value against the last value,
        // if the difference is outside the tolerance, Ditto should update
        if (dittoPoint.containsKey(TOLERANCE) && dittoPoint.getDouble(TOLERANCE) >= 0d) {
            if (Strings.isNumeric(newValue.toString()) &&
                Math.abs(Double.parseDouble(newValue.toString()) - dittoPoint.getDouble(LAST_VALUE)) >
                dittoPoint.getDouble(TOLERANCE)) {
                return true;
            }
        } else {
            if (dittoPoint.getDouble(LAST_VALUE) != newValue) {
                return true;
            }
        }

        // If the point has a tolerance, check each value in the old priority array against the new one,
        // if the difference of any are outside the tolerance, Ditto should update
        if (dittoPoint.containsKey(TOLERANCE) && dittoPoint.getDouble(TOLERANCE) >= 0d) {
            if (!JsonUtils.compareJsonObject(oldPriorityArray, priorityArray)) {
                for (int i = 1; i < 16; i++) {
                    if (Math.abs(priorityArray.getDouble(Integer.toString(i))) -
                        oldPriorityArray.getDouble(Integer.toString(i)) > dittoPoint.getDouble(TOLERANCE)) {
                        return true;
                    }
                }
            }
        } else {
            return oldPriorityArray.encode().equals(priorityArray.encode());
        }
        return false;
    }

    boolean isEnoughTimeToUpdateDitto(JsonObject dittoPoint) {
        return Duration.between(DateTimes.fromUTC(Instant.ofEpochMilli(dittoPoint.getLong(LAST_UPDATED))),
                                DateTimes.nowUTC()).toMillis() > POST_RATE;
    }

}
