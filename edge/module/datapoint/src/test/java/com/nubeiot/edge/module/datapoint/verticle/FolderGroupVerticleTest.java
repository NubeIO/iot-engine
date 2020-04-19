package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.zero.utils.UUID64;
import io.vertx.core.http.HttpMethod;
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
        final JsonObject expected = new JsonObject(
            "{\"folders\":[{\"id\":\"107fw3juRaGEio82gMtRUQ\",\"folder\":{\"id\":\"8ODBGHhLRsmVzSJ3pdZRYw\"," +
            "\"name\":\"folder-2\"}},{\"id\":\"8P6qsXSmQPyXQWw6vXarNA\"," +
            "\"point_id\":\"1efaf662-1333-48d1-a60f-8fc60f259f0e\",\"folder\":{\"id\":\"JWssmgTOSYWlYaZGUXlsSw\"," +
            "\"name\":\"folder-1\"}},{\"id\":\"_IuzgpsuT7uXHZ0PeXh5xQ\"," +
            "\"folder\":{\"id\":\"JWssmgTOSYWlYaZGUXlsSw\",\"name\":\"folder-1\"}}]}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) + "/folder", 200, expected,
                           JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_list_folders_by_point(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"folders\":[{\"id\":\"8P6qsXSmQPyXQWw6vXarNA\",\"folder\":{\"id\":\"JWssmgTOSYWlYaZGUXlsSw\"," +
            "\"name\":\"folder-1\"}}]}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/point/" + UUID64.uuidToBase64(PrimaryKey.P_GPIO_TEMP) + "/folder", 200, expected);
    }

    @Test
    public void test_list_object_by_folder(TestContext context) {
        final String path = "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) + "/folder/" +
                            PrimaryKey.FOLDER_1 + "/point";
        assertRestByClient(context, HttpMethod.GET, path, 200, new JsonObject());
    }

}
