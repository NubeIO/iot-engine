package com.nubeiot.edge.connector.bonescript.operations;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO_ENABLE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO_HOST;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO_HTTP_BASIC;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.FEATURES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.LAST_UPDATED;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.LAST_VALUE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.POINTS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PROPERTIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.SETTINGS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.TOLERANCE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.UI1;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;

import java.net.URL;
import java.time.Instant;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.FileUtilsTest;

import io.vertx.core.json.JsonObject;

public class DittoTest extends TestBase {

    private static final URL DITTO_EXAMPLE_RESOURCE = FileUtilsTest.class.getClassLoader()
                                                                         .getResource("ditto/ditto_example.json");
    private String dittoHost = "http://localhost:8080";
    private String dittoHttpBasic = "xyz";

    @Before
    public void setUp() {
        super.setUp();
        Ditto.init(new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString())));
    }

    @Test
    public void test_init_updateDittoEnable() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        updateDittoEnable(db, true);
        Ditto.init(db);
        Assert.assertTrue(Ditto.getInstance().getDittoEnable());
        updateDittoEnable(db, false);
        Ditto.init(db);
        Assert.assertFalse(Ditto.getInstance().getDittoEnable());
    }

    @Test
    public void test_init_updateDittoHost() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        updateDittoHost(db, dittoHost);
        Ditto.init(db);
        Assert.assertEquals(Ditto.getInstance().getDittoHost(), dittoHost);
    }

    @Test
    public void test_init_updateDittoHttpBasic() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        updateDittoHttpBasic(db, dittoHttpBasic);
        Ditto.init(db);
        Assert.assertEquals(Ditto.getInstance().getDittoHttpBasic(), dittoHttpBasic);
    }

    @Test
    public void test_isEnoughTimeToUpdateDitto() {
        Assert.assertFalse(Ditto.getInstance()
                                .isEnoughTimeToUpdateDitto(
                                    new JsonObject().put(LAST_UPDATED, Instant.now().getEpochSecond() * 1000)));
        Assert.assertTrue(Ditto.getInstance()
                               .isEnoughTimeToUpdateDitto(new JsonObject().put(LAST_UPDATED,
                                                                               Instant.now().getEpochSecond() * 1000 -
                                                                               Ditto.getInstance().POST_RATE)));
    }

    @Test
    public void test_shouldDittoUpdate_onDittoEnableFalse() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        JsonObject point = db.getJsonObject(THING)
                             .getJsonObject(FEATURES)
                             .getJsonObject(POINTS)
                             .getJsonObject(PROPERTIES)
                             .getJsonObject(UI1);
        updateDittoEnable(db, false);
        Ditto.init(db);
        Assert.assertFalse(Ditto.getInstance().shouldDittoUpdate(point, VALUE, 1d, new JsonObject()));
    }

    @Test
    public void test_shouldDittoUpdateWithNewValue_onDittoEnableWithDifferentPriorityArray() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        JsonObject point = db.getJsonObject(THING)
                             .getJsonObject(FEATURES)
                             .getJsonObject(POINTS)
                             .getJsonObject(PROPERTIES)
                             .getJsonObject(UI1);
        updateDittoEnable(db, true);
        Ditto.init(db);
        Assert.assertTrue(Ditto.getInstance().shouldDittoUpdateWithNewValue(point, 1d, new JsonObject()));
    }

    @Test
    public void test_shouldDittoUpdateWithNewValue_onDittoEnableWithSamePriorityArray() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        JsonObject point = db.getJsonObject(THING)
                             .getJsonObject(FEATURES)
                             .getJsonObject(POINTS)
                             .getJsonObject(PROPERTIES)
                             .getJsonObject(UI1);
        updateDittoEnable(db, true);
        updateDittoTolerance(point, 1d);
        updateDittoLastValue(point, 2d);
        Ditto.init(db);
        Assert.assertFalse(Ditto.getInstance().shouldDittoUpdateWithNewValue(point, 1.1d, null));
        Assert.assertTrue(Ditto.getInstance().shouldDittoUpdateWithNewValue(point, 0.9d, null));
        Assert.assertFalse(Ditto.getInstance().shouldDittoUpdateWithNewValue(point, "null", null));
    }

    private void updateDittoEnable(JsonObject db, boolean enable) {
        db.getJsonObject(THING)
          .getJsonObject(FEATURES)
          .getJsonObject(SETTINGS)
          .getJsonObject(PROPERTIES)
          .put(DITTO_ENABLE, enable);
    }

    private void updateDittoHost(JsonObject db, String host) {
        db.getJsonObject(THING)
          .getJsonObject(FEATURES)
          .getJsonObject(SETTINGS)
          .getJsonObject(PROPERTIES)
          .put(DITTO_HOST, host);
    }

    private void updateDittoHttpBasic(JsonObject db, String dittoHttpBasic) {
        db.getJsonObject(THING)
          .getJsonObject(FEATURES)
          .getJsonObject(SETTINGS)
          .getJsonObject(PROPERTIES)
          .put(DITTO_HTTP_BASIC, dittoHttpBasic);
    }

    private void updateDittoTolerance(JsonObject point, double tolerance) {
        point.getJsonObject(DITTO).put(TOLERANCE, tolerance);
    }

    private void updateDittoLastValue(JsonObject point, double lastValue) {
        point.getJsonObject(DITTO).put(LAST_VALUE, lastValue);
    }

}
