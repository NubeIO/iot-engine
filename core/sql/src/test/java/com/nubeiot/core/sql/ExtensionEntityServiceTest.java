package com.nubeiot.core.sql;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;

@RunWith(VertxUnitRunner.class)
public class ExtensionEntityServiceTest extends BaseSqlServiceTest {

    @BeforeClass
    public static void beforeSuite() { BaseSqlTest.beforeSuite(); }

    @Test
    public void test_get_list_without_another(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"books\":[{\"id\":1,\"author_id\":1,\"title\":\"1984\",\"published_in\":\"1947-12-31T17:01Z\"," +
            "\"language_id\":1},{\"id\":2,\"author_id\":1,\"title\":\"Animal Farm\"," +
            "\"published_in\":\"1944-12-31T17:01Z\",\"language_id\":1},{\"id\":3,\"author_id\":2,\"title\":\"O " +
            "Alquimista\",\"published_in\":\"1987-12-31T18:01Z\",\"language_id\":4},{\"id\":4,\"author_id\":2," +
            "\"title\":\"Brida\",\"published_in\":\"1989-12-31T18:01Z\",\"language_id\":2}]}");
        asserter(context, true, expected, BOOK_ADDRESS, EventAction.GET_LIST, RequestData.builder().build());
    }

    @Test
    public void test_get_list_by_another(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"books\":[{\"id\":1,\"title\":\"1984\",\"published_in\":\"1947-12-31T17:01Z\"," +
            "\"language_id\":1},{\"id\":2,\"title\":\"Animal Farm\"," +
            "\"published_in\":\"1944-12-31T17:01Z\",\"language_id\":1}]}");
        asserter(context, true, expected, BOOK_ADDRESS, EventAction.GET_LIST,
                 RequestData.builder().body(new JsonObject().put("author_id", 1)).build());
    }

    @Test
    public void test_get_one_by_another_success(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"id\":2,\"language_id\":1,\"title\":\"Animal Farm\"," + "\"published_in\":\"1944-12-31T17:01Z\"}");
        RequestData reqData = RequestData.builder()
                                         .body(new JsonObject().put("author_id", "1").put("book_id", "2"))
                                         .build();
        asserter(context, true, expected, BOOK_ADDRESS, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_get_one_by_another_not_found(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with book_id=2\"}");
        RequestData reqData = RequestData.builder()
                                         .body(new JsonObject().put("author_id", "2").put("book_id", "2"))
                                         .build();
        asserter(context, false, expected, BOOK_ADDRESS, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_get_one_without_another_success(TestContext context) {
        JsonObject expected = new JsonObject("{\"id\":2,\"language_id\":1,\"author_id\":1,\"title\":\"Animal Farm\"," +
                                             "\"published_in\":\"1944-12-31T17:01Z\"}");
        RequestData reqData = RequestData.builder().body(new JsonObject().put("book_id", "2")).build();
        asserter(context, true, expected, BOOK_ADDRESS, EventAction.GET_ONE, reqData);
    }

    @Test
    public void test_patch_by_another_success(TestContext context) {
        JsonObject expected = new JsonObject("{\"resource\":{\"id\":2,\"author_id\":1,\"title\":\"Farm\"," +
                                             "\"published_in\":\"1944-12-31T17:01Z\",\"language_id\":1}," +
                                             "\"action\":\"PATCH\",\"status\":\"SUCCESS\"}");
        RequestData reqData = RequestData.builder()
                                         .body(new JsonObject().put("author_id", 1)
                                                               .put("book_id", 2)
                                                               .put("title", "Farm"))
                                         .build();
        asserter(context, true, expected, BOOK_ADDRESS, EventAction.PATCH, reqData);
    }

}
