package com.nubeiot.edge.module.datapoint.verticle;

import java.util.UUID;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
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
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;

public class PointVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_points_with_invalid_key_400(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/network/xxx/point/" + PrimaryKey.P_GPIO_TEMP, 400,
                           new JsonObject("{\"message\":\"Invalid key\",\"code\":\"INVALID_ARGUMENT\"}"));
    }

    @Test
    public void test_get_list_tags_by_point_200(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "/tags", 200,
                           new JsonObject("{\"tags\":[{\"id\":1,\"tag_name\":\"sensor\",\"tag_value\":\"temp\"}," +
                                          "{\"id\":2,\"tag_name\":\"source\",\"tag_value\":\"droplet\"}]}"));
    }

    @Test
    public void test_get_tag_by_point_and_id_200(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "/tags/2", 200,
                           new JsonObject("{\"id\":2,\"tag_name\":\"source\",\"tag_value\":\"droplet\"}"));
    }

    @Test
    public void test_get_list_tags_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"tags\":[{\"id\":1,\"tag_name\":\"sensor\",\"point\":\"1efaf662-1333-48d1-a60f-8fc60f259f0e\"," +
            "\"tag_value\":\"temp\"},{\"id\":2,\"tag_name\":\"source\"," +
            "\"point\":\"1efaf662-1333-48d1-a60f-8fc60f259f0e\"," +
            "\"tag_value\":\"droplet\"},{\"id\":3,\"tag_name\":\"sensor\"," +
            "\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\"," +
            "\"tag_value\":\"temp\"},{\"id\":4,\"tag_name\":\"source\"," +
            "\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\",\"tag_value\":\"hvac\"}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/tags", 200, expected);
    }

    @Test
    public void test_get_tag_by_id_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"id\":1,\"tag_name\":\"sensor\",\"point\":\"1efaf662-1333-48d1-a60f-8fc60f259f0e\"," +
            "\"tag_value\":\"temp\"}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/tags/1", 200, expected);
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
    public void test_get_point_incl_tags_and_his_setting_200(TestContext context) {
        final JsonObject point = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        point.remove("measure_unit");
        point.put("unit", Temperature.CELSIUS.toJson());
        point.put("history_setting", new JsonObject(
            "{\"point\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"type\":\"COV\",\"tolerance\":1.0,\"enabled\":false}"));
        point.put("tags", new JsonArray(
            "[{\"id\":1,\"tag_name\":\"sensor\",\"point\":\"" + PrimaryKey.P_GPIO_TEMP + "\"," +
            "\"tag_value\":\"temp\"},{\"id\":2,\"tag_name\":\"source\",\"point\":\"" + PrimaryKey.P_GPIO_TEMP +
            "\",\"tag_value\":\"droplet\"}]"));
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "?_incl=tag,history_setting", 200, point);
    }

}
