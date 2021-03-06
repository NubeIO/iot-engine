package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestFilter.Filters;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.unit.DataTypeCategory.Base;
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;

public class PointServiceReaderTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_point_by_edge(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP))
                                      .toJson()
                                      .put("unit", Temperature.CELSIUS.toJson());
        expected.remove("measure_unit");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("edge_id", PrimaryKey.EDGE.toString())
                                                           .put("point_id", PrimaryKey.P_GPIO_TEMP.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_point_by_network(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP))
                                      .toJson()
                                      .put("unit", Temperature.CELSIUS.toJson());
        expected.remove("measure_unit");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("network_id", "local")
                                                           .put("point_id", PrimaryKey.P_GPIO_TEMP.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_list_point_by_filter_network_id(TestContext context) {
        final JsonObject p1 = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_HUMIDITY)).toJson();
        final JsonObject p2 = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        final JsonObject expected = new JsonObject().put("points", new JsonArray().add(p1).add(p2));
        final RequestData req = RequestData.builder()
                                           .filter(
                                               new JsonObject().put("network", PrimaryKey.DEFAULT_NETWORK.toString()))
                                           .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_LIST, req,
                 JSONCompareMode.LENIENT);
    }

    @Test
    public void test_get_list_point_by_filter_network_alias(TestContext context) {
        final JsonObject p1 = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_HUMIDITY)).toJson();
        final JsonObject p2 = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        final JsonObject expected = new JsonObject().put("points", new JsonArray().add(p1).add(p2));
        final RequestData req = RequestData.builder().filter(new JsonObject().put("network", "default")).build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_LIST, req,
                 JSONCompareMode.LENIENT);
    }

    @Test
    public void test_get_list_point_by_network_alias(TestContext context) {
        final JsonObject p1 = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_HUMIDITY)).toJson();
        final JsonObject p2 = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        final JsonObject expected = new JsonObject().put("points", new JsonArray().add(p1).add(p2));
        final RequestData req = RequestData.builder().body(new JsonObject().put("network_id", "default")).build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_LIST, req,
                 JSONCompareMode.LENIENT);
    }

    @Test
    public void test_get_list_point_by_network_id(TestContext context) {
        final JsonObject p1 = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_TEMP)).toJson();
        final JsonObject p2 = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_FAN)).toJson();
        final JsonObject p3 = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_SWITCH)).toJson();
        final JsonObject expected = new JsonObject().put("points", new JsonArray().add(p1).add(p2).add(p3));
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("network_id", PrimaryKey.BACNET_NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_LIST, req,
                 JSONCompareMode.LENIENT);
    }

    @Test
    public void test_get_point_by_network_not_found(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message",
                                                   "Not found resource with point_id=" + PrimaryKey.P_GPIO_TEMP);
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("network_id", PrimaryKey.BACNET_NETWORK.toString())
                                                           .put("point_id", PrimaryKey.P_GPIO_TEMP.toString()))
                                     .build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_point_by_edge_and_default_network(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_HUMIDITY))
                                      .toJson()
                                      .put("unit", Base.PERCENTAGE.toJson());
        expected.remove("measure_unit");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("edge_id", PrimaryKey.EDGE.toString())
                                                           .put("network_id", "default")
                                                           .put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_point_by_edge_and_another_network(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_TEMP))
                                      .toJson()
                                      .put("unit", Temperature.CELSIUS.toJson());
        expected.remove("measure_unit");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("edge_id", PrimaryKey.EDGE.toString())
                                                           .put("network_id", PrimaryKey.BACNET_NETWORK.toString())
                                                           .put("point_id", PrimaryKey.P_BACNET_TEMP.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_point_by_edge_and_another_network_not_found(TestContext context) {
        JsonObject expected = new JsonObject("{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with point_id=" +
                                             PrimaryKey.P_BACNET_FAN.toString() + "\"}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("edge_id", PrimaryKey.EDGE.toString())
                                                           .put("network_id", "default")
                                                           .put("point_id", PrimaryKey.P_BACNET_FAN.toString()))
                                     .build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_list_tag_by_point(TestContext context) {
        JsonObject expected = new JsonObject("{\"tags\":[{\"id\":3,\"tag_name\":\"sensor\",\"tag_value\":\"temp\"}," +
                                             "{\"id\":4,\"tag_name\":\"source\",\"tag_value\":\"hvac\"}]}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_BACNET_TEMP.toString()))
                                     .build();
        asserter(context, true, expected, TagPointService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_one_tag_by_point(TestContext context) {
        JsonObject expected = new JsonObject("{\"id\":3,\"tag_name\":\"sensor\",\"tag_value\":\"temp\"}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_BACNET_TEMP.toString())
                                                           .put("tag_id", 3))
                                     .build();
        asserter(context, true, expected, TagPointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_tag_by_tagName(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"tags\":[{\"id\":1,\"tag_name\":\"sensor\",\"point\":\"" + PrimaryKey.P_GPIO_TEMP.toString() + "\"," +
            "\"tag_value\":\"temp\"},{\"id\":3,\"tag_name\":\"sensor\",\"point\":\"" +
            PrimaryKey.P_BACNET_TEMP.toString() + "\",\"tag_value\":\"temp\"}]}");
        RequestData req = RequestData.builder().filter(new JsonObject().put("tag_name", "sensor")).build();
        asserter(context, true, expected, TagPointService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_tag_by_id(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"id\":1,\"tag_name\":\"sensor\",\"point\":\"" + PrimaryKey.P_GPIO_TEMP.toString() + "\"," +
            "\"tag_value\":\"temp\"}");
        RequestData req = RequestData.builder().body(new JsonObject().put("tag_id", "1")).build();
        asserter(context, true, expected, TagPointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_history_setting(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"tolerance\":1.0,\"type\":\"COV\",\"point\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"enabled\":false}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_TEMP.toString()))
                                     .build();
        asserter(context, true, expected, HistorySettingService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_history_setting_not_found(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message",
                                                   "Not found resource with point_id=" + PrimaryKey.P_GPIO_HUMIDITY);
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, false, expected, HistorySettingService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_list_point_by_advance_query_in_list_code(TestContext context) {
        final Point p1 = MockData.search(PrimaryKey.P_BACNET_FAN);
        final Point p2 = MockData.search(PrimaryKey.P_BACNET_TEMP);
        final JsonObject filter = new JsonObject().put(Filters.QUERY,
                                                       "code=in=(" + p1.getCode() + "," + p2.getCode() + ")");
        final RequestData reqData = RequestData.builder().filter(filter).build();
        final JsonObject expected = new JsonObject().put("points", new JsonArray().add(JsonPojo.from(p1).toJson())
                                                                                  .add(JsonPojo.from(p2).toJson()));
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_LIST, reqData,
                 JSONCompareMode.LENIENT);
    }

    @Test
    public void test_get_list_point_by_advance_query_or_comparision(TestContext context) {
        final Point p1 = MockData.search(PrimaryKey.P_BACNET_FAN);
        final Point p2 = MockData.search(PrimaryKey.P_BACNET_TEMP);
        final Point p3 = MockData.search(PrimaryKey.P_BACNET_SWITCH);
        final Point p4 = MockData.search(PrimaryKey.P_GPIO_TEMP);
        final JsonObject filter = new JsonObject().put(Filters.QUERY, "protocol==BACNET or measure_unit==celsius");
        final RequestData reqData = RequestData.builder().filter(filter).build();
        final JsonObject expected = new JsonObject().put("points", new JsonArray().add(JsonPojo.from(p1).toJson())
                                                                                  .add(JsonPojo.from(p2).toJson())
                                                                                  .add(JsonPojo.from(p3).toJson())
                                                                                  .add(JsonPojo.from(p4).toJson()));
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_LIST, reqData,
                 JSONCompareMode.LENIENT);
    }

}
