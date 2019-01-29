package com.nubeiot.edge.connector.bonescript.operations;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DITTO;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.FEATURES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.LAST_UPDATED;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.POINTS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PROPERTIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.TOLERANCE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.UI1;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;

import java.net.URL;
import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.FileUtilsTest;
import com.nubeiot.core.utils.JsonUtils;

import io.vertx.core.json.JsonObject;

public class DittoTest extends TestBase {

    private static final URL DITTO_EXAMPLE_RESOURCE = FileUtilsTest.class.getClassLoader()
                                                                         .getResource("ditto/ditto_example.json");

    @Test
    public void test_init_updateDittoEnable() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        Ditto.getInstance(db);
        Assert.assertEquals(Ditto.getInstance().getDittoEnable(),
                            JsonUtils.getObject(db, "thing.features.settings.properties.dittoEnable"));
    }

    @Test
    public void test_init_updateDittoHost() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        Ditto.getInstance(db);
        Assert.assertEquals(Ditto.getInstance().getDittoHost(),
                            JsonUtils.getObject(db, "thing.features.settings.properties.dittoHost"));
    }

    @Test
    public void test_init_updateDittoHttpBasic() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        Ditto.getInstance(db);
        Assert.assertEquals(Ditto.getInstance().getDittoHost(),
                            JsonUtils.getObject(db, "thing.features.settings.properties.dittoHttpBasic"));
    }

    @Test
    public void test_isEnoughTimeToUpdateDitto() {
        Ditto.getInstance(new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString())));
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
        Ditto.getInstance(new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString())));
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        JsonObject point = db.getJsonObject(THING)
                             .getJsonObject(FEATURES)
                             .getJsonObject(POINTS)
                             .getJsonObject(PROPERTIES)
                             .getJsonObject(UI1);
        Assert.assertFalse(Ditto.getInstance().shouldDittoUpdate(point, VALUE, 1d, new JsonObject()));
    }

    @Test
    public void test_shouldDittoUpdateWithNewValue_onDifferentValue() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        Ditto.getInstance(db);
        JsonObject point = db.getJsonObject(THING)
                             .getJsonObject(FEATURES)
                             .getJsonObject(POINTS)
                             .getJsonObject(PROPERTIES)
                             .getJsonObject(UI1);
        Assert.assertTrue(Ditto.getInstance().shouldDittoUpdateWithNewValue(point, 1d, null));
    }

    @Test
    public void test_shouldDittoUpdateWithNewValue_onDifferentToleranceLevel() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        JsonObject point = db.getJsonObject(THING)
                             .getJsonObject(FEATURES)
                             .getJsonObject(POINTS)
                             .getJsonObject(PROPERTIES)
                             .getJsonObject(UI1);
        point.getJsonObject(DITTO).put(TOLERANCE, 50);
        Ditto.getInstance(db);
        Assert.assertFalse(Ditto.getInstance().shouldDittoUpdateWithNewValue(point, 1.1d, null));
    }

}
