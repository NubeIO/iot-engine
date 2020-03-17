package com.nubeiot.edge.module.datapoint.verticle;

import java.util.UUID;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

public class PointVerticleWriterTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_create_point_201(TestContext context) {
        final UUID uuid = UUID.randomUUID();
        final Point search = MockData.search(PrimaryKey.P_BACNET_TEMP).setId(uuid).setCode("New One");
        final JsonObject json = JsonPojo.from(search).toJson().put("unit", new JsonObject().put("type", "bool"));
        json.remove("measure_unit");
        assertRestByClient(context, HttpMethod.POST, "/api/s/point", RequestData.builder().body(json).build(), 201,
                           new JsonObject().put("action", EventAction.CREATE)
                                           .put("status", Status.SUCCESS)
                                           .put("resource", new JsonObject(
                                               "{\"id\":\"" + uuid + "\",\"code\":\"New One\",\"edge\":\"" +
                                               PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.BACNET_NETWORK +
                                               "\",\"enabled\":true,\"protocol\":\"BACNET\",\"kind\":\"INPUT\"," +
                                               "\"type\":\"DIGITAL\",\"precision\":3,\"offset\":0," +
                                               "\"unit\":{\"type\":\"bool\",\"category\":\"ALL\"}}")));
    }

    @Test
    public void test_create_point_incl_history_setting_201(TestContext context) {
        final UUID uuid = UUID.randomUUID();
        final Point search = MockData.search(PrimaryKey.P_BACNET_TEMP).setId(uuid).setCode("New One");
        final JsonObject hisSetting = new JsonObject("{\"type\":\"COV\",\"tolerance\":1.0,\"enabled\":false}");
        final JsonObject json = JsonPojo.from(search)
                                        .toJson()
                                        .put("unit", new JsonObject().put("type", "bool"))
                                        .put("history_setting", hisSetting);
        json.remove("measure_unit");
        final JsonObject response = new JsonObject(
            "{\"id\":\"" + uuid + "\",\"code\":\"New One\",\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" +
            PrimaryKey.BACNET_NETWORK + "\",\"enabled\":true,\"protocol\":\"BACNET\",\"kind\":\"INPUT\"," +
            "\"type\":\"DIGITAL\",\"precision\":3,\"offset\":0,\"unit\":{\"type\":\"bool\",\"category\":\"ALL\"}}").put(
            "history_setting", hisSetting.put("point", uuid.toString()));
        assertRestByClient(context, HttpMethod.POST, "/api/s/point", RequestData.builder().body(json).build(), 201,
                           new JsonObject().put("action", EventAction.CREATE)
                                           .put("status", Status.SUCCESS)
                                           .put("resource", response));
    }

}
