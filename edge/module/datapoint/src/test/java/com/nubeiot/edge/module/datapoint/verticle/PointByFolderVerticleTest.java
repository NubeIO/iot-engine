package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.github.zero.utils.UUID64;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class PointByFolderVerticleTest extends FolderGroupVerticleTest {

    @Test
    public void test_list_points_in_network_n_device_n_folder(TestContext context) {
        final String path = "/api/s/network/default/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) +
                            "/folder/" + PrimaryKey.FOLDER_3 + "/point";
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":\"" + PrimaryKey.FOLDER_GROUP_4 + "\",\"point\":{\"id\":\"" + PrimaryKey.P_GPIO_TEMP +
            "\",\"code\":\"2CB2B763_TEMP\",\"edge\":\"" + PrimaryKey.EDGE + "\"," + "\"network\":\"" +
            PrimaryKey.DEFAULT_NETWORK + "\",\"enabled\":true,\"protocol\":\"WIRE\",\"kind\":\"INPUT\"," +
            "\"type\":\"DIGITAL\",\"measure_unit\":\"celsius\",\"precision\":3,\"offset\":0}},{\"id\":\"" +
            PrimaryKey.FOLDER_GROUP_5 + "\",\"point\":{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY +
            "\",\"code\":\"2CB2B763_HUMIDITY\",\"edge\":\"" + PrimaryKey.EDGE + "\"," + "\"network\":\"" +
            PrimaryKey.DEFAULT_NETWORK + "\",\"enabled\":true,\"protocol\":\"WIRE\",\"kind\":\"INPUT\"," +
            "\"type\":\"DIGITAL\",\"measure_unit\":\"percentage\",\"min_scale\":0,\"max_scale\":100," +
            "\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET, path, 200, expected);
    }

    @Test
    public void test_list_points_in_device_n_folder(TestContext context) {
        final String path = "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) + "/folder/" +
                            PrimaryKey.FOLDER_4 + "/point";
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":\"" + PrimaryKey.FOLDER_GROUP_6 + "\",\"point\":{\"id\":\"" + PrimaryKey.P_GPIO_TEMP +
            "\",\"code\":\"2CB2B763_TEMP\",\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" +
            PrimaryKey.DEFAULT_NETWORK + "\",\"enabled\":true,\"protocol\":\"WIRE\",\"kind\":\"INPUT\"," +
            "\"type\":\"DIGITAL\",\"measure_unit\":\"celsius\",\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET, path, 200, expected);
    }

    @Test
    public void test_list_points_in_folder(TestContext context) {
        final String path = "/api/s/folder/" + PrimaryKey.FOLDER_4 + "/point";
        final JsonObject expected = new JsonObject(
            "{\"points\":[{\"id\":\"" + PrimaryKey.FOLDER_GROUP_6 + "\"," + "\"point\":{\"id\":\"" +
            PrimaryKey.P_GPIO_TEMP + "\",\"code\":\"2CB2B763_TEMP\",\"edge\":\"" + PrimaryKey.EDGE +
            "\",\"network\":\"" + PrimaryKey.DEFAULT_NETWORK +
            "\",\"enabled\":true,\"protocol\":\"WIRE\",\"kind\":\"INPUT\"," +
            "\"type\":\"DIGITAL\",\"measure_unit\":\"celsius\",\"precision\":3,\"offset\":0}}]}");
        assertRestByClient(context, HttpMethod.GET, path, 200, expected);
    }

    @Test
    public void test_get_point_in_folder(TestContext context) {
        final String path = "/api/s/folder/" + PrimaryKey.FOLDER_3 + "/point/" + PrimaryKey.P_GPIO_HUMIDITY;
        final JsonObject expected = new JsonObject(
            "{\"point\":{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\",\"code\":\"2CB2B763_HUMIDITY\"," +
            "\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.DEFAULT_NETWORK + "\"," +
            "\"enabled\":true,\"protocol\":\"WIRE\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"percentage\",\"min_scale\":0,\"max_scale\":100,\"precision\":3,\"offset\":0}," +
            "\"id\":\"" + PrimaryKey.FOLDER_GROUP_5 + "\"}");
        assertRestByClient(context, HttpMethod.GET, path, 200, expected);
    }

    @Test
    public void test_get_point_in_device_n_folder(TestContext context) {
        final String path = "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/folder/" + PrimaryKey.FOLDER_3 +
                            "/point/" + PrimaryKey.P_GPIO_TEMP;
        final JsonObject expected = new JsonObject(
            "{\"point\":{\"id\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"code\":\"2CB2B763_TEMP\",\"edge\":\"" +
            PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.DEFAULT_NETWORK + "\",\"enabled\":true," +
            "\"protocol\":\"WIRE\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\",\"measure_unit\":\"celsius\"," +
            "\"precision\":3,\"offset\":0}," + "\"id\":\"" + PrimaryKey.FOLDER_GROUP_4 + "\"}");
        assertRestByClient(context, HttpMethod.GET, path, 200, expected);
    }

    @Test
    public void test_get_point_in_device_n_folder_not_found(TestContext context) {
        final String path = "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/folder/" + PrimaryKey.FOLDER_4 +
                            "/point/" + PrimaryKey.P_GPIO_HUMIDITY;
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message", "Not found resource with folder_id=" +
                                                                    UUID64.uuid64ToUuidStr(PrimaryKey.FOLDER_4) +
                                                                    " and device_id=" + PrimaryKey.DEVICE_DROPLET +
                                                                    " and point_id=" + PrimaryKey.P_GPIO_HUMIDITY);
        assertRestByClient(context, HttpMethod.GET, path, 404, expected);
    }

    @Test
    public void test_attach_existed_point_into_folder(TestContext context) {
        final String path = "/api/s/folder/" + PrimaryKey.FOLDER_4 + "/point";
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("point_id", PrimaryKey.P_BACNET_TEMP.toString()))
                                           .build();
        assertRestByClient(context, HttpMethod.POST, path, req, 400, new JsonObject());
    }

    @Test
    public void test_reattach_existed_point_in_device_folder(TestContext context) {
        final String path = "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/folder/" + PrimaryKey.FOLDER_3 + "/point";
        final RequestData r = RequestData.builder()
                                         .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                         .build();
        final JsonObject resp = new JsonObject().put("code", ErrorCode.ALREADY_EXIST)
                                                .put("message", "Already existed resource with folder_id=" +
                                                                UUID64.uuid64ToUuidStr(PrimaryKey.FOLDER_3) + " and " +
                                                                "device_id=" + PrimaryKey.DEVICE_DROPLET +
                                                                " and network_id=" + PrimaryKey.DEFAULT_NETWORK +
                                                                " and point_id=" + PrimaryKey.P_GPIO_HUMIDITY);
        assertRestByClient(context, HttpMethod.POST, path, r, 422, resp);
    }

    @Test
    public void test_attach_existed_point_in_device_folder(TestContext context) {
        final String path = "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/folder/" + PrimaryKey.FOLDER_4 + "/point";
        final RequestData r = RequestData.builder()
                                         .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                         .build();
        final JsonObject resp = new JsonObject().put("code", ErrorCode.ALREADY_EXIST)
                                                .put("message", "Already existed resource with folder_id=" +
                                                                UUID64.uuid64ToUuidStr(PrimaryKey.FOLDER_3) + " and " +
                                                                "device_id=" + PrimaryKey.DEVICE_DROPLET + " and " +
                                                                "network_id=" + PrimaryKey.DEFAULT_NETWORK + " and " +
                                                                "point_id=" + PrimaryKey.P_GPIO_HUMIDITY);
        assertRestByClient(context, HttpMethod.POST, path, r, 201, resp);
    }

}