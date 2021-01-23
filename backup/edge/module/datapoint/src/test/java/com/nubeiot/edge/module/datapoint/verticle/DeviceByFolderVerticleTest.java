package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class DeviceByFolderVerticleTest extends FolderGroupVerticleTest {

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

    @Test
    public void test_list_device_in_network_folder(TestContext context) {
        final String path = "/api/s/network/default/folder/" + PrimaryKey.FOLDER_1 + "/device";
        final JsonObject expected = new JsonObject(
            "{\"devices\":[{\"id\":\"" + PrimaryKey.FOLDER_GROUP_1 + "\",\"device\":{\"id\":\"" +
            PrimaryKey.DEVICE_DROPLET + "\",\"code\":\"DROPLET_01\",\"protocol\":\"WIRE\",\"type\":\"DROPLET\"," +
            "\"state\":\"NONE\",\"manufacturer\":\"NubeIO\"}}]}");
        assertRestByClient(context, HttpMethod.GET, path, 200, expected);
    }

    @Test
    public void test_list_device_in_invalid_folder_level(TestContext context) {
        final String path = "/api/s/folder/" + PrimaryKey.FOLDER_2 + "/device";
        final JsonObject expected = new JsonObject("{\"devices\":[]}");
        assertRestByClient(context, HttpMethod.GET, path, 200, expected);
    }

}