package com.nubeiot.edge.connector.bonescript.handlers;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.ID;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRIORITY;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRIORITY_ARRAY;
import static com.nubeiot.edge.connector.bonescript.operations.DittoTest.DITTO_EXAMPLE_RESOURCE;
import static com.nubeiot.edge.connector.bonescript.validations.PointUpdateValidationTest.VALIDATED_POINT_UPDATE;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.edge.connector.bonescript.BoneScriptEventBus;
import com.nubeiot.edge.connector.bonescript.operations.Ditto;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public class PointEventHandlerTest {

    private PointsEventHandler pointsEventHandler;

    @Before
    public void setUp() {
        Vertx vertx = Vertx.vertx();
        pointsEventHandler = new PointsEventHandler(vertx, BoneScriptEventBus.POINTS);
    }

    @Test
    public void test_patchPoint() {
        JsonObject db = new JsonObject(FileUtils.readFileToString(DITTO_EXAMPLE_RESOURCE.toString()));
        JsonArray newPoints = new JsonArray(FileUtils.readFileToString(VALIDATED_POINT_UPDATE.toString()));
        Ditto.getInstance(db);

        JsonObject points = (JsonObject) JsonUtils.getObject(db, "thing.features.points.properties");
        AtomicBoolean updateDitto = new AtomicBoolean(false);
        JsonObject newPoint = newPoints.getJsonObject(0);
        String id = newPoints.getJsonObject(0).getString(ID);

        pointsEventHandler.patchPoint(points, updateDitto, newPoint, id);

        Assert.assertEquals(
            points.getJsonObject(id).getJsonObject(PRIORITY_ARRAY).getValue(newPoint.getValue(PRIORITY).toString()),
            newPoint.getValue("value"));
        Assert.assertFalse(updateDitto.get());
    }

}
