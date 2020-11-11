package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.zero88.utils.UUID64;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Folder;

public class FolderByDeviceVerticleTest extends FolderGroupVerticleTest {

    @Test
    public void test_list_folders_in_device(TestContext context) {
        final String path = "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) + "/folder";
        JsonArray arr = new JsonArray().add(new JsonObject().put("id", PrimaryKey.FOLDER_2).put("name", "folder-2"))
                                       .add(new JsonObject().put("id", PrimaryKey.FOLDER_3).put("name", "folder-3"))
                                       .add(new JsonObject().put("id", PrimaryKey.FOLDER_4).put("name", "folder-4"));
        assertRestByClient(context, HttpMethod.GET, path, 200, new JsonObject().put("folders", arr),
                           JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_get_folder_in_device(TestContext context) {
        final String path = "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) + "/folder/" +
                            PrimaryKey.FOLDER_2;
        assertRestByClient(context, HttpMethod.GET, path, 200,
                           new JsonObject().put("id", PrimaryKey.FOLDER_2).put("name", "folder-2"),
                           JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_create_folder_has_id_in_device(TestContext context) {
        final String path = "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) + "/folder";
        final String id = UUID64.random();
        final JsonObject folder = new JsonObject().put("name", "folder-xx").put("id", id);
        final RequestData reqData = RequestData.builder().body(new JsonObject().put("folder", folder)).build();
        final Folder expected = new Folder().setId(id).setName("folder-xx");
        assertRestByClient(context, HttpMethod.POST, path, reqData, 201,
                           EntityTransformer.fullResponse(EventAction.CREATE, JsonPojo.from(expected).toJson()));
    }

    @Test
    public void test_create_folder_no_id_in_device(TestContext context) {
        final String path = "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) + "/folder";
        final JsonObject folder = new JsonObject().put("name", "folder-no-id");
        final RequestData reqData = RequestData.builder().body(new JsonObject().put("folder", folder)).build();
        assertRestByClient(context, HttpMethod.POST, path, reqData, 201,
                           EntityTransformer.fullResponse(EventAction.CREATE, folder),
                           JsonHelper.ignore("resource.id"));
    }

    @Test
    public void test_create_unique_folder_name_in_device(TestContext context) {
        final String path = "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) + "/folder";
        final String id = UUID64.random();
        final JsonObject folder = new JsonObject().put("name", "folder-1").put("id", id);
        final RequestData reqData = RequestData.builder().body(new JsonObject().put("folder", folder)).build();
        assertRestByClient(context, HttpMethod.POST, path, reqData, 422,
                           new JsonObject().put("code", ErrorCode.ALREADY_EXIST)
                                           .put("message", "Already existed resource with name=folder-1"));
    }

    @Test
    public void test_delete_folder_in_device(TestContext context) {
        final String path = "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET) + "/folder/" +
                            PrimaryKey.FOLDER_2;
        assertRestByClient(context, HttpMethod.DELETE, path, 204, new JsonObject());
    }

    @Test
    public void test_unable_delete_folder_is_assigned_directly(TestContext context) {
        final String path = "/api/s/folder/" + PrimaryKey.FOLDER_2;
        final JsonObject expected = new JsonObject().put("code", ErrorCode.BEING_USED)
                                                    .put("message", "Resource with folder_id=" +
                                                                    UUID64.uuid64ToUuidStr(PrimaryKey.FOLDER_2) +
                                                                    " is using by another resource");
        assertRestByClient(context, HttpMethod.DELETE, path, 422, expected);
    }

    @Test
    public void test_delete_folder_is_assigned_directly_with_force(TestContext context) {
        final String path = "/api/s/folder/" + PrimaryKey.FOLDER_2 + "?_force";
        assertRestByClient(context, HttpMethod.DELETE, path, 204, new JsonObject());
    }

}