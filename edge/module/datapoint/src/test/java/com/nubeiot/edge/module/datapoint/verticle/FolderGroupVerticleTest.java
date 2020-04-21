package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.zero.utils.UUID64;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class FolderGroupVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Folder_Group();
    }

    @Test
    public void test_list_folders_by_device(TestContext context) {
        JsonArray arr = new JsonArray().add(new JsonObject().put("id", PrimaryKey.FOLDER_2).put("name", "folder-2"))
                                       .add(new JsonObject().put("id", PrimaryKey.FOLDER_3).put("name", "folder-3"))
                                       .add(new JsonObject().put("id", PrimaryKey.FOLDER_4).put("name", "folder-4"));
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) + "/folder", 200,
                           new JsonObject().put("folders", arr), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_list_folders_that_point_belongs_to(TestContext context) {
        JsonArray arr = new JsonArray().add(new JsonObject().put("id", PrimaryKey.FOLDER_3).put("name", "folder-3"))
                                       .add(new JsonObject().put("id", PrimaryKey.FOLDER_4).put("name", "folder-4"));
        assertRestByClient(context, HttpMethod.GET, "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "/folder", 200,
                           new JsonObject().put("folders", arr));
    }

    @Test
    public void test_list_points_by_network_n_device_n_folder(TestContext context) {
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
    public void test_list_point_in_folder(TestContext context) {
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
    public void test_list_device_in_folder(TestContext context) {
        final String path = "/api/s/folder/" + PrimaryKey.FOLDER_1 + "/device";
        final JsonObject expected = new JsonObject(
            "{\"devices\":[{\"id\":\"" + PrimaryKey.FOLDER_GROUP_1 + "\",\"device\":{\"id\":\"" +
            PrimaryKey.DEVICE_DROPLET + "\",\"code\":\"DROPLET_01\",\"protocol\":\"WIRE\",\"type\":\"DROPLET\"," +
            "\"state\":\"NONE\",\"manufacturer\":\"NubeIO\"}},{\"id\":\"" + PrimaryKey.FOLDER_GROUP_2 +
            "\",\"device\":{\"id\":\"" + PrimaryKey.DEVICE_HVAC + "\"," +
            "\"code\":\"HVAC_XYZ\",\"protocol\":\"BACNET\",\"type\":\"HVAC\"," +
            "\"state\":\"NONE\",\"manufacturer\":\"Lennox\"}}]}");
        assertRestByClient(context, HttpMethod.GET, path, 200, expected);
    }

}
