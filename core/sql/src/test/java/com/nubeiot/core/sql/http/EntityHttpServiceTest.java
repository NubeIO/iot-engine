package com.nubeiot.core.sql.http;

import java.util.Set;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.service.MockEntityService.Metadata.AuthorMetadata;
import com.nubeiot.core.sql.service.MockEntityService.Metadata.BookMetadata;

public class EntityHttpServiceTest {

    @Test
    public void test_one_resource() throws JSONException {
        final Set<EventMethodDefinition> definitions = EntityHttpService.createCRUDDefinitions(AuthorMetadata.INSTANCE);
        final JsonObject expected = new JsonObject(
            "{\"data\":[{\"servicePath\":\"/author\",\"mapping\":[{\"action\":\"UPDATE\",\"method\":\"PUT\"," +
            "\"capturePath\":\"/author/:author_id\",\"regexPath\":\"/author/.+\"},{\"action\":\"PATCH\"," +
            "\"method\":\"PATCH\",\"capturePath\":\"/author/:author_id\",\"regexPath\":\"/author/.+\"}," +
            "{\"action\":\"GET_LIST\",\"method\":\"GET\",\"capturePath\":\"/author\",\"regexPath\":\"/author\"}," +
            "{\"action\":\"REMOVE\",\"method\":\"DELETE\",\"capturePath\":\"/author/:author_id\"," +
            "\"regexPath\":\"/author/.+\"},{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"capturePath\":\"/author/:author_id\",\"regexPath\":\"/author/.+\"},{\"action\":\"CREATE\"," +
            "\"method\":\"POST\",\"capturePath\":\"/author\",\"regexPath\":\"/author\"}],\"useRequestData\":true}]}");
        JsonHelper.assertJson(expected, JsonData.tryParse(definitions).toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    public void test_one_resource_none_full_crud() throws JSONException {
        final Set<EventAction> events = ActionMethodMapping.defaultReadMap().keySet();
        final Set<EventMethodDefinition> definitions = EntityHttpService.createDefinitions(ActionMethodMapping.CRUD_MAP,
                                                                                           events,
                                                                                           AuthorMetadata.INSTANCE);
        final JsonObject expected = new JsonObject(
            "{\"data\":[{\"servicePath\":\"/author\",\"mapping\":[{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"capturePath\":\"/author/:author_id\",\"regexPath\":\"/author/.+\"},{\"action\":\"GET_LIST\"," +
            "\"method\":\"GET\",\"capturePath\":\"/author\",\"regexPath\":\"/author\"}],\"useRequestData\":true}]}");
        JsonHelper.assertJson(expected, JsonData.tryParse(definitions).toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    public void test_one_resource_one_reference() throws JSONException {
        final Set<EventMethodDefinition> definitions = EntityHttpService.createDefinitions(ActionMethodMapping.READ_MAP,
                                                                                           BookMetadata.INSTANCE,
                                                                                           AuthorMetadata.INSTANCE);
        final JsonObject expected = new JsonObject(
            "{\"data\":[{\"servicePath\":\"/author/[^/]+/book\",\"mapping\":[{\"action\":\"GET_ONE\"," +
            "\"method\":\"GET\",\"capturePath\":\"/author/:author_id/book/:book_id\"," +
            "\"regexPath\":\"/author/[^/]+/book/.+\"},{\"action\":\"GET_LIST\",\"method\":\"GET\"," +
            "\"capturePath\":\"/author/:author_id/book\",\"regexPath\":\"/author/[^/]+/book\"}]," +
            "\"useRequestData\":true}]}");
        JsonHelper.assertJson(expected, JsonData.tryParse(definitions).toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    public void test_one_resource_more_than_one_reference() throws JSONException {
        final Set<EventMethodDefinition> definitions = EntityHttpService.createDefinitions(ActionMethodMapping.READ_MAP,
                                                                                           AuthorMetadata.INSTANCE,
                                                                                           AuthorMetadata.INSTANCE,
                                                                                           BookMetadata.INSTANCE);
        final JsonObject expected = new JsonObject(
            "{\"data\":[{\"servicePath\":\"/author/[^/]+/book/[^/]+/author\",\"mapping\":[{\"action\":\"GET_ONE\"," +
            "\"method\":\"GET\",\"capturePath\":\"/author/:author_id/book/:book_id/author/:author_id\"," +
            "\"regexPath\":\"/author/[^/]+/book/[^/]+/author/.+\"},{\"action\":\"GET_LIST\",\"method\":\"GET\"," +
            "\"capturePath\":\"/author/:author_id/book/:book_id/author\"," +
            "\"regexPath\":\"/author/[^/]+/book/[^/]+/author\"}],\"useRequestData\":true}," +
            "{\"servicePath\":\"/author/[^/]+/author\",\"mapping\":[{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"capturePath\":\"/author/:author_id/author/:author_id\",\"regexPath\":\"/author/[^/]+/author/.+\"}," +
            "{\"action\":\"GET_LIST\",\"method\":\"GET\",\"capturePath\":\"/author/:author_id/author\"," +
            "\"regexPath\":\"/author/[^/]+/author\"}],\"useRequestData\":true}," +
            "{\"servicePath\":\"/book/[^/]+/author\",\"mapping\":[{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"capturePath\":\"/book/:book_id/author/:author_id\",\"regexPath\":\"/book/[^/]+/author/.+\"}," +
            "{\"action\":\"GET_LIST\",\"method\":\"GET\",\"capturePath\":\"/book/:book_id/author\"," +
            "\"regexPath\":\"/book/[^/]+/author\"}],\"useRequestData\":true}]}");
        JsonHelper.assertJson(expected, JsonData.tryParse(definitions).toJson(), JSONCompareMode.LENIENT);
    }

}
