package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.zero.utils.UUID64;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class FolderByPointVerticleTest extends FolderGroupVerticleTest {

    @Test
    public void test_list_folders_that_point_belongs_to(TestContext context) {
        final String path = "/api/s/point/" + UUID64.uuidToBase64(PrimaryKey.P_GPIO_TEMP) + "/folder";
        JsonArray arr = new JsonArray().add(new JsonObject().put("id", PrimaryKey.FOLDER_3).put("name", "folder-3"))
                                       .add(new JsonObject().put("id", PrimaryKey.FOLDER_4).put("name", "folder-4"));
        assertRestByClient(context, HttpMethod.GET, path, 200, new JsonObject().put("folders", arr),
                           JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_get_folder_that_point_belongs_to(TestContext context) {
        final String path = "/api/s/point/" + UUID64.uuidToBase64(PrimaryKey.P_GPIO_HUMIDITY) + "/folder/" +
                            PrimaryKey.FOLDER_3;
        assertRestByClient(context, HttpMethod.GET, path, 200,
                           new JsonObject().put("id", PrimaryKey.FOLDER_3).put("name", "folder-3"));
    }

    @Test
    public void test_create_new_folder_then_attach_by_point(TestContext context) {
        final String folderId = UUID64.random();
        final String path = "/api/s/point/" + PrimaryKey.P_GPIO_HUMIDITY + "/folder";
        final JsonObject folder = new JsonObject().put("id", folderId).put("name", "folder-p-test");
        final RequestData reqData = RequestData.builder()
                                               .body(new JsonObject().put("folder", folder)
                                                                     .put("device_id",
                                                                          PrimaryKey.DEVICE_DROPLET.toString()))
                                               .build();
        assertRestByClient(context, HttpMethod.POST, path, reqData, 201,
                           EntityTransformer.fullResponse(EventAction.CREATE, folder));
    }

    @Test
    public void test_attach_existed_point_into_existed_folder(TestContext context) {
        final String path = "/api/s/point/" + PrimaryKey.P_GPIO_HUMIDITY + "/folder";
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("folder_id", PrimaryKey.FOLDER_4)
                                                                 .put("device_id",
                                                                      PrimaryKey.DEVICE_DROPLET.toString()))
                                           .build();
        final JsonObject resp = EntityTransformer.fullResponse(EventAction.CREATE,
                                                               new JsonObject().put("id", PrimaryKey.FOLDER_4)
                                                                               .put("name", "folder-4"));
        assertRestByClient(context, HttpMethod.POST, path, req, 201, resp);
    }

}