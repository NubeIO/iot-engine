package com.nubeio.iot.share.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class RequestDataTest {

    @Test
    public void test_to_json() {
        RequestData requestData = RequestData.builder()
                                             .pagination(Pagination.builder().build())
                                             .body(new JsonObject().put("name", "hello"))
                                             .filter(new JsonObject().put("filter", "test"))
                                             .build();
        assertEquals("hello", requestData.getBody().getString("name"));
        assertEquals("test", requestData.getFilter().getString("filter"));
        assertEquals(1, requestData.getPagination().getPage());
        assertEquals(20, requestData.getPagination().getPerPage());
        assertEquals("{\"body\":{\"name\":\"hello\"},\"filter\":{\"filter\":\"test\"},\"pagination\":{\"page\":1," +
                     "\"perPage\":20}}", requestData.toJson().encode());
    }

    @Test
    public void test_from_json_1() {
        final JsonObject pagination = new JsonObject().put("page", 5).put("perPage", 10);
        final JsonObject data = new JsonObject().put("pagination", pagination)
                                                .put("body", new JsonObject())
                                                .put("filter", (String) null);
        final RequestData requestData = data.mapTo(RequestData.class);
        assertEquals("{}", requestData.getBody().encode());
        assertEquals("{}", requestData.getFilter().encode());
        assertNotNull(requestData.getPagination());
        assertEquals(5, requestData.getPagination().getPage());
        assertEquals(10, requestData.getPagination().getPerPage());
    }

    @Test
    public void test_from_json_2() {
        final JsonObject pagination = new JsonObject().put("page", 5).put("perPage", 10);
        final JsonObject data = new JsonObject().put("pagination", pagination)
                                                .put("body", new JsonObject().put("name", "xyz"))
                                                .put("filter", new JsonObject().put("key", "1"));
        JsonObject body = Json.mapper.convertValue(data.getJsonObject("body").encode(), JsonObject.class);
        assertEquals("xyz", body.getString("name"));
        //        assertEquals("{}", requestData.getBody().encode());
        //        assertNull(requestData.getFilter());
        //        assertNotNull(requestData.getPagination());
        //        assertEquals(5, requestData.getPagination().getPage());
        //        assertEquals(10, requestData.getPagination().getPerPage());
    }

}