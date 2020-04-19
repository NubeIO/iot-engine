package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.zero.utils.UUID64;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Folder;

public class FolderServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Folder_Group();
    }

    @Test
    public void test_list_folder(TestContext context) {
        final JsonArray array = MockData.FOLDERS.stream()
                                                .collect(JsonArray::new, (arr, f) -> arr.add(JsonPojo.from(f).toJson()),
                                                         JsonArray::addAll);
        final JsonObject expected = new JsonObject().put("folders", array);
        asserter(context, true, expected, FolderService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_get_folder_invalid_key(TestContext context) {
        final JsonObject expected = new JsonObject("{\"code\":\"INVALID_ARGUMENT\",\"message\":\"Invalid key\"}");
        final RequestData reqData = RequestData.builder().body(new JsonObject().put("folder_id", 2)).build();
        asserter(context, false, expected, FolderService.class.getName(), EventAction.GET_ONE, reqData,
                 JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_get_folder_success(TestContext context) {
        final JsonObject expected = new JsonObject("{\"name\":\"folder-2\",\"id\":\"" + PrimaryKey.FOLDER_2 + "\"}");
        final RequestData reqData = RequestData.builder()
                                               .body(new JsonObject().put("folder_id", PrimaryKey.FOLDER_2))
                                               .build();
        asserter(context, true, expected, FolderService.class.getName(), EventAction.GET_ONE, reqData,
                 JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_get_folder_by_edge(TestContext context) {
        final JsonObject expected = new JsonObject("{\"name\":\"folder-1\",\"id\":\"" + PrimaryKey.FOLDER_1 + "\"}");
        final RequestData reqData = RequestData.builder()
                                               .body(
                                                   new JsonObject().put("edge_id", UUID64.uuidToBase64(PrimaryKey.EDGE))
                                                                   .put("folder_id", PrimaryKey.FOLDER_1))
                                               .build();
        asserter(context, true, expected, FolderService.class.getName(), EventAction.GET_ONE, reqData,
                 JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_create_implicit_edge(TestContext context) {
        final String id = UUID64.random();
        final Folder folder = new Folder().setName("folder 3").setId(id);
        asserter(context, true, createResponseExpected(folder), FolderService.class.getName(), EventAction.CREATE,
                 RequestData.builder().body(JsonPojo.from(folder).toJson()).build());
    }

    @Test
    public void test_create_explicit_invalid_edge(TestContext context) {
        final UUID invalidEdge = UUID.randomUUID();
        final Folder folder = new Folder().setName("folder 3").setEdgeId(invalidEdge);
        asserter(context, false, new JsonObject(
                     "{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with edge_id=" + invalidEdge + "\"}"),
                 FolderService.class.getName(), EventAction.CREATE,
                 RequestData.builder().body(JsonPojo.from(folder).toJson()).build());
    }

    private JsonObject createResponseExpected(Folder response) {
        return new JsonObject().put("action", EventAction.CREATE)
                               .put("status", Status.SUCCESS)
                               .put("resource", JsonPojo.from(response).toJson());
    }

}