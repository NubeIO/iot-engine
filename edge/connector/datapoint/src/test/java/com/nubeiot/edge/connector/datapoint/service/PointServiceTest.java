package com.nubeiot.edge.connector.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.JsonPojo;
import com.nubeiot.edge.connector.datapoint.MockData;
import com.nubeiot.edge.connector.datapoint.MockData.PrimaryKey;

public class PointServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_point_by_device(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE.toString())
                                                           .put("point_id", PrimaryKey.P_GPIO_TEMP.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_point_by_network(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("network_id", "GPIO")
                                                           .put("point_id", PrimaryKey.P_GPIO_TEMP.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_list_point_by_default_network(TestContext context) {
        final JsonObject p1 = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_HUMIDITY)).toJson();
        final JsonObject p2 = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_TEMP)).toJson();
        final JsonObject expected = new JsonObject().put("points", new JsonArray().add(p1).add(p2));
        RequestData req = RequestData.builder().body(new JsonObject().put("network_id", "GPIO")).build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_list_point_by_another_network(TestContext context) {
        final JsonObject p1 = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_TEMP)).toJson();
        final JsonObject p2 = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_FAN)).toJson();
        final JsonObject p3 = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_SWITCH)).toJson();
        final JsonObject expected = new JsonObject().put("points", new JsonArray().add(p1).add(p2).add(p3));
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("network_id", PrimaryKey.NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_point_by_network_not_found(TestContext context) {
        JsonObject expected = new JsonObject("{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with point_id=" +
                                             PrimaryKey.P_GPIO_TEMP.toString() + "\"}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("network_id", PrimaryKey.NETWORK.toString())
                                                           .put("point_id", PrimaryKey.P_GPIO_TEMP.toString()))
                                     .build();
        asserter(context, false, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_point_by_device_and_default_network(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.search(PrimaryKey.P_GPIO_HUMIDITY)).toJson();
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE.toString())
                                                           .put("network_id", "GPIO")
                                                           .put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_point_by_device_and_another_network(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.search(PrimaryKey.P_BACNET_TEMP)).toJson();
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE.toString())
                                                           .put("network_id", PrimaryKey.NETWORK.toString())
                                                           .put("point_id", PrimaryKey.P_BACNET_TEMP.toString()))
                                     .build();
        asserter(context, true, expected, PointService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_point_by_device_and_another_network_not_found(TestContext context) {
        JsonObject expected = new JsonObject("{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with point_id=" +
                                             PrimaryKey.P_BACNET_FAN.toString() + "\"}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE.toString())
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
        JsonObject expected = new JsonObject("{\"schedule\":\"xxxx\",\"type\":\"PERIOD\"}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_TEMP.toString()))
                                     .build();
        asserter(context, true, expected, HistorySettingService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_history_setting_not_found(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with point_id=" + PrimaryKey.P_GPIO_HUMIDITY +
            "\"}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, false, expected, HistorySettingService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_history_data_by_point(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"histories\":[{\"id\":1,\"time\":\"2019-08-10T09:15Z\",\"value\":30.0},{\"id\":2," +
            "\"time\":\"2019-08-10T09:18Z\",\"value\":35.0},{\"id\":3,\"time\":\"2019-08-10T09:20Z\",\"value\":32.0}," +
            "{\"id\":4,\"time\":\"2019-08-10T09:22Z\",\"value\":42.0}]}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, true, expected, HistoryDataService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_history_data(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"histories\":[{\"id\":1,\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\"," +
            "\"time\":\"2019-08-10T09:15Z\",\"value\":30.0},{\"id\":2," +
            "\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\",\"time\":\"2019-08-10T09:18Z\",\"value\":35.0}," +
            "{\"id\":3,\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\",\"time\":\"2019-08-10T09:20Z\",\"value\":32" +
            ".0},{\"id\":4,\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\",\"time\":\"2019-08-10T09:22Z\"," +
            "\"value\":42.0},{\"id\":5,\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\"," +
            "\"time\":\"2019-08-10T09:15:15Z\",\"value\":20.5},{\"id\":6," +
            "\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\",\"time\":\"2019-08-10T09:16:15Z\",\"value\":20.8}," +
            "{\"id\":7,\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\",\"time\":\"2019-08-10T09:17:15Z\"," +
            "\"value\":20.8},{\"id\":8,\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\"," +
            "\"time\":\"2019-08-10T09:18:15Z\",\"value\":20.6}]}");
        RequestData req = RequestData.builder().build();
        asserter(context, true, expected, HistoryDataService.class.getName(), EventAction.GET_LIST, req);
    }

}
