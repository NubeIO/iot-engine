package com.nubeiot.edge.module.datapoint.verticle;

import java.util.UUID;

import org.junit.Test;

import io.github.zero88.utils.UUID64;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.unit.DataType;

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
    public void test_get_point_in_network_n_device_n_folder(TestContext context) {
        final String path = "/api/s/network/default/device/" + PrimaryKey.DEVICE_DROPLET + "/folder/" +
                            PrimaryKey.FOLDER_3 + "/point/" + PrimaryKey.P_GPIO_TEMP;
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
    public void test_attach_point_into_folder_without_device(TestContext context) {
        final String path = "/api/s/folder/" + PrimaryKey.FOLDER_4 + "/point";
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("point_id", PrimaryKey.P_BACNET_TEMP.toString()))
                                           .build();
        assertRestByClient(context, HttpMethod.POST, path, req, 400,
                           new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                           .put("message", "device_id is mandatory"));
    }

    @Test
    public void test_attach_point_into_folder_with_device(TestContext context) {
        final String path = "/api/s/folder/" + PrimaryKey.FOLDER_4 + "/point";
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString())
                                                           .put("device_id", PrimaryKey.DEVICE_DROPLET.toString()))
                                     .build();
        final JsonObject resp = EntityTransformer.fullResponse(EventAction.CREATE, new JsonObject(
            "{\"id\":\"--\",\"point\":{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\",\"code\":\"2CB2B763_HUMIDITY\"," +
            "\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.DEFAULT_NETWORK + "\",\"label\":null," +
            "\"enabled\":true,\"protocol\":\"WIRE\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"percentage\",\"unit_alias\":null,\"min_scale\":0,\"max_scale\":100,\"precision\":3," +
            "\"offset\":0,\"version\":null,\"metadata\":null}}"));
        assertRestByClient(context, HttpMethod.POST, path, req, 201, resp, JsonHelper.ignore("resource.id"));
    }

    @Test
    public void test_attach_unmatched_point_n_device_into_folder(TestContext context) {
        final String path = "/api/s/folder/" + PrimaryKey.FOLDER_4 + "/point";
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("point_id", PrimaryKey.P_BACNET_TEMP.toString())
                                                                 .put("device_id",
                                                                      PrimaryKey.DEVICE_DROPLET.toString()))
                                           .build();
        assertRestByClient(context, HttpMethod.POST, path, req, 409,
                           new JsonObject().put("code", ErrorCode.CONFLICT_ERROR)
                                           .put("message", "Point id " + PrimaryKey.P_BACNET_TEMP +
                                                           " does not belongs to device id " +
                                                           PrimaryKey.DEVICE_DROPLET));
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
                                                                "device_id=" + PrimaryKey.DEVICE_DROPLET + " and " +
                                                                "network_id=" + PrimaryKey.DEFAULT_NETWORK + " and " +
                                                                "point_id=" + PrimaryKey.P_GPIO_HUMIDITY);
        assertRestByClient(context, HttpMethod.POST, path, r, 422, resp);
    }

    @Test
    public void test_attach_existed_point_in_existed_device_folder(TestContext context) {
        final String path = "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/folder/" + PrimaryKey.FOLDER_4 + "/point";
        final RequestData r = RequestData.builder()
                                         .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                         .build();
        final JsonObject resp = EntityTransformer.fullResponse(EventAction.CREATE, new JsonObject(
            "{\"id\":\"--\",\"point\":{\"id\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\",\"code\":\"2CB2B763_HUMIDITY\"," +
            "\"edge\":\"" + PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.DEFAULT_NETWORK + "\"," +
            "\"label\":null,\"enabled\":true,\"protocol\":\"WIRE\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\"," +
            "\"measure_unit\":\"percentage\",\"unit_alias\":null,\"min_scale\":0,\"max_scale\":100,\"precision\":3," +
            "\"offset\":0,\"version\":null,\"metadata\":null}}"));
        assertRestByClient(context, HttpMethod.POST, path, r, 201, resp, JsonHelper.ignore("resource.id"));
    }

    @Test
    public void test_attach_non_existed_point_in_existed_device_folder(TestContext context) {
        final String path = "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/folder/" + PrimaryKey.FOLDER_4 + "/point";
        final RequestData r = RequestData.builder()
                                         .body(new JsonObject().put("point_id", PrimaryKey.DEVICE_HVAC.toString()))
                                         .build();
        final JsonObject resp = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                .put("message",
                                                     "Not found resource with point_id=" + PrimaryKey.DEVICE_HVAC);
        assertRestByClient(context, HttpMethod.POST, path, r, 410, resp);
    }

    @Test
    public void test_create_and_attach_new_point_in_existed_device_folder(TestContext context) {
        final String path = "/api/s/device/" + PrimaryKey.DEVICE_DROPLET + "/folder/" + PrimaryKey.FOLDER_4 + "/point";
        final Point point = new Point().setId(UUID.randomUUID())
                                       .setCode("TEST-1")
                                       .setEdge(PrimaryKey.EDGE)
                                       .setNetwork(PrimaryKey.DEFAULT_NETWORK)
                                       .setMeasureUnit(DataType.def().type());
        final RequestData r = RequestData.builder()
                                         .body(new JsonObject().put("point", JsonPojo.from(point).toJson()))
                                         .build();
        final JsonObject resp = EntityTransformer.fullResponse(EventAction.CREATE, new JsonObject(
            "{\"id\":\"--\",\"point\":{\"id\":\"" + point.getId() + "\",\"code\":\"TEST-1\",\"edge\":\"" +
            PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.DEFAULT_NETWORK + "\",\"label\":null,\"enabled\":true," +
            "\"protocol\":\"UNKNOWN\",\"kind\":\"UNKNOWN\",\"type\":\"UNKNOWN\",\"measure_unit\":\"number\"," +
            "\"unit_alias\":null,\"min_scale\":null,\"max_scale\":null,\"precision\":null,\"offset\":null," +
            "\"version\":null,\"metadata\":null}}"));
        assertRestByClient(context, HttpMethod.POST, path, r, 201, resp, JsonHelper.ignore("resource.id"));
    }

}