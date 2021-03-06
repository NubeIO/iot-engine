package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;

public class PointVerticleReaderTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_point_with_invalid_key_400(TestContext context) {
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
    public void test_get_point_incl_tags_and_his_setting_200(TestContext context) {
        final JsonObject point = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        point.remove("measure_unit");
        point.put("unit", Temperature.CELSIUS.toJson());
        point.put("history_setting", new JsonObject(
            "{\"point\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"type\":\"COV\",\"tolerance\":1.0,\"enabled\":false}"));
        point.put("tags", new JsonArray("[{\"id\":1,\"tag_name\":\"sensor\",\"tag_value\":\"temp\"}," +
                                        "{\"id\":2,\"tag_name\":\"source\",\"tag_value\":\"droplet\"}]"));
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "?_incl" + "=tag,history_setting", 200, point);
    }

    @Test
    public void test_get_point_incl_his_setting_200(TestContext context) {
        final JsonObject point = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        point.remove("measure_unit");
        point.put("unit", Temperature.CELSIUS.toJson());
        point.put("history_setting", new JsonObject(
            "{\"point\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"type\":\"COV\",\"tolerance\":1.0,\"enabled\":false}"));
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "?_incl=history_setting,realtime_setting", 200,
                           point);
    }

    @Test
    public void test_get_point_incl_rt_setting_200(TestContext context) {
        final JsonObject point = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        point.remove("measure_unit");
        point.put("unit", Temperature.CELSIUS.toJson());
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "?_incl=realtime_setting", 200, point);
    }

    @Test
    public void test_get_point_incl_tags_200(TestContext context) {
        final JsonObject point = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        point.remove("measure_unit");
        point.put("unit", Temperature.CELSIUS.toJson());
        point.put("tags", new JsonArray("[{\"id\":1,\"tag_name\":\"sensor\",\"tag_value\":\"temp\"}," +
                                        "{\"id\":2,\"tag_name\":\"source\",\"tag_value\":\"droplet\"}]"));
        assertRestByClient(context, HttpMethod.GET, "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "?_incl=tag", 200,
                           point);
    }

    @Test
    public void test_get_points_by_advance_query(TestContext context) {
        final Point search1 = MockData.search(PrimaryKey.P_GPIO_TEMP);
        final Point search2 = MockData.search(PrimaryKey.P_GPIO_HUMIDITY);
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":\"1efaf662-1333-48d1-a60f-8fc60f259f0e\",\"code\":\"2CB2B763_TEMP\"," +
            "\"edge\":\"d7cd3f57-a188-4462-b959-df7a23994c92\",\"network\":\"e3eab951-932e-4fcc-a925-08b31e1014a0\"," +
            "\"enabled\":true,\"protocol\":\"WIRE\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"celsius\",\"precision\":3,\"offset\":0}," +
            "{\"id\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\",\"code\":\"2CB2B763_HUMIDITY\"," +
            "\"edge\":\"d7cd3f57-a188-4462-b959-df7a23994c92\",\"network\":\"e3eab951-932e-4fcc-a925-08b31e1014a0\"," +
            "\"enabled\":true,\"protocol\":\"WIRE\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"percentage\",\"min_scale\":0,\"max_scale\":100,\"precision\":3,\"offset\":0}]}");
        final String query = "_incl=tag&_q=code=in=(" + search1.getCode() + "," + search2.getCode() + ")" +
                             "&_page=1&_per_page=2";
        assertRestByClient(context, HttpMethod.GET, "/api/s/point?" + query, 200, expected);
    }

}
