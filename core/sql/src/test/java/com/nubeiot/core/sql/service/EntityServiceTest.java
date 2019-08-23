package com.nubeiot.core.sql.service;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.mock.oneschema.tables.pojos.Author;
import com.nubeiot.core.sql.pojos.JsonPojo;

@RunWith(VertxUnitRunner.class)
public class EntityServiceTest extends BaseSqlServiceTest {

    @Test
    public void test_get_list_without_filter(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"authors\":[{\"id\":1,\"first_name\":\"George\",\"last_name\":\"Orwell\"," +
            "\"date_of_birth\":\"1903-06-26\",\"distinguished\":true},{\"id\":2,\"first_name\":\"Paulo\"," +
            "\"last_name\":\"Coelho\",\"date_of_birth\":\"1947-08-24\",\"distinguished\":false}]}");
        asserter(context, true, expected, AUTHOR_ADDRESS, EventAction.GET_LIST, RequestData.builder().build());
    }

    @Test
    public void test_get_list_with_filter(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"authors\":[{\"id\":1,\"first_name\":\"George\",\"last_name\":\"Orwell\"," +
            "\"date_of_birth\":\"1903-06-26\",\"distinguished\":true}]}");
        final RequestData reqData = RequestData.builder().filter(new JsonObject().put("first_name", "George")).build();
        asserter(context, true, expected, AUTHOR_ADDRESS, EventAction.GET_LIST, reqData);
    }

    @Test
    public void test_get_list_with_filter_no_result(TestContext context) {
        JsonObject expected = new JsonObject("{\"authors\":[]}");
        final RequestData reqData = RequestData.builder().filter(new JsonObject().put("first_name", "xxx")).build();
        asserter(context, true, expected, AUTHOR_ADDRESS, EventAction.GET_LIST, reqData);
    }

    @Test
    public void test_get_one(TestContext context) {
        JsonObject expected = new JsonObject("{\"id\":1,\"first_name\":\"George\",\"last_name\":\"Orwell\"," +
                                             "\"date_of_birth\":\"1903-06-26\",\"distinguished\":true}");
        RequestData reqData = RequestData.builder().body(new JsonObject().put("author_id", "1")).build();
        asserter(context, true, expected, AUTHOR_ADDRESS, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_get_one_not_found(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with author_id=3\"}");
        RequestData reqData = RequestData.builder().body(new JsonObject().put("author_id", "3")).build();
        asserter(context, false, expected, AUTHOR_ADDRESS, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_create_one(TestContext context) {
        JsonObject expected = new JsonObject("{\"resource\":{\"id\":3,\"first_name\":\"ab\",\"last_name\":\"xyz\"," +
                                             "\"date_of_birth\":\"1935-07-30\",\"distinguished\":null}," +
                                             "\"action\":\"CREATE\",\"status\":\"SUCCESS\"}");
        RequestData reqData = RequestData.builder()
                                         .body(new Author().setFirstName("ab")
                                                           .setLastName("xyz")
                                                           .setDateOfBirth(LocalDate.of(1935, 7, 30))
                                                           .toJson())
                                         .build();
        asserter(context, true, expected, AUTHOR_ADDRESS, EventAction.CREATE, reqData);
    }

    @Test
    public void test_create_one_failed(TestContext context) {
        JsonObject expected = new JsonObject("{\"code\":\"INVALID_ARGUMENT\",\"message\":\"last_name is mandatory\"}");
        RequestData reqData = RequestData.builder().body(new Author().toJson()).build();
        asserter(context, false, expected, AUTHOR_ADDRESS, EventAction.CREATE, reqData);
    }

    @Test
    public void test_update_one(TestContext context) throws InterruptedException {
        JsonObject expected = new JsonObject("{\"resource\":{\"id\":1,\"first_name\":\"ab\",\"last_name\":\"xyz\"," +
                                             "\"date_of_birth\":\"1980-03-08\"},\"action\":\"UPDATE\"," +
                                             "\"status\":\"SUCCESS\"}");
        RequestData reqData = RequestData.builder()
                                         .body(new Author().setFirstName("ab")
                                                           .setLastName("xyz")
                                                           .setDateOfBirth(LocalDate.of(1980, 3, 8))
                                                           .toJson()
                                                           .put("author_id", 1))
                                         .build();
        CountDownLatch latch = new CountDownLatch(1);
        asserter(context, true, expected, AUTHOR_ADDRESS, EventAction.UPDATE, reqData, latch);
        expected = new JsonObject(
            "{\"id\":1,\"first_name\":\"ab\",\"last_name\":\"xyz\",\"date_of_birth\":\"1980-03-08\"}");
        reqData = RequestData.builder().body(new JsonObject().put("author_id", "1")).build();
        latch.await(TestHelper.TEST_TIMEOUT_SEC / 3, TimeUnit.SECONDS);
        asserter(context, true, expected, AUTHOR_ADDRESS, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_patch_one(TestContext context) throws InterruptedException {
        JsonObject expected = new JsonObject("{\"resource\":{\"id\":2,\"first_name\":\"ab\",\"last_name\":\"Coelho\"," +
                                             "\"date_of_birth\":\"1947-08-24\",\"distinguished\":false}," +
                                             "\"action\":\"PATCH\",\"status\":\"SUCCESS\"}");
        final JsonObject body = JsonPojo.from(new Author().setFirstName("ab")).toJson().put("author_id", 2);
        RequestData reqData = RequestData.builder().body(body).build();
        CountDownLatch latch = new CountDownLatch(1);
        asserter(context, true, expected, AUTHOR_ADDRESS, EventAction.PATCH, reqData, latch);
        expected = new JsonObject("{\"id\":2,\"first_name\":\"ab\",\"last_name\":\"Coelho\"," +
                                  "\"date_of_birth\":\"1947-08-24\",\"distinguished\":false}");
        reqData = RequestData.builder().body(new JsonObject().put("author_id", "2")).build();
        latch.await(TestHelper.TEST_TIMEOUT_SEC / 2, TimeUnit.SECONDS);
        asserter(context, true, expected, AUTHOR_ADDRESS, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_delete_one(TestContext context) {
        JsonObject expected = new JsonObject("{\"resource\":{\"id\":1,\"author_id\":1,\"title\":\"1984\"," +
                                             "\"published_in\":\"1947-12-31T17:01Z\",\"language_id\":1}," +
                                             "\"action\":\"REMOVE\",\"status\":\"SUCCESS\"}");
        RequestData reqData = RequestData.builder().body(new JsonObject().put("book_id", "1")).build();
        asserter(context, true, expected, BOOK_ADDRESS, EventAction.REMOVE, reqData);
    }

    @Test
    public void test_delete_one_not_found(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with author_id=3\"}");
        RequestData reqData = RequestData.builder().body(new JsonObject().put("author_id", "3")).build();
        asserter(context, false, expected, AUTHOR_ADDRESS, EventAction.REMOVE, reqData);
    }

}
