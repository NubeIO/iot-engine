package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class PointVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_list_tags_by_point(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "/tags", 200,
                           new JsonObject("{\"tags\":[{\"id\":1,\"tag_name\":\"sensor\",\"tag_value\":\"temp\"}," +
                                          "{\"id\":2,\"tag_name\":\"source\",\"tag_value\":\"droplet\"}]}"));
    }

    @Test
    public void test_get_tag_by_point_and_id(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "/tags/2", 200,
                           new JsonObject("{\"id\":2,\"tag_name\":\"source\",\"tag_value\":\"droplet\"}"));
    }

    @Test
    public void test_get_list_tags(TestContext context) {
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
    public void test_get_tag_by_id(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"id\":1,\"tag_name\":\"sensor\",\"point\":\"1efaf662-1333-48d1-a60f-8fc60f259f0e\"," +
            "\"tag_value\":\"temp\"}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/tags/1", 200, expected);
    }

}
