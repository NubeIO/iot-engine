package com.nubeiot.core.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

public class RequestDataTest {

    @Test
    public void test_to_json() {
        RequestData requestData = RequestData.builder()
                                             .pagination(Pagination.builder().build())
                                             .body(new JsonObject().put("name", "hello"))
                                             .filter(new JsonObject().put("x", "test"))
                                             .build();
        assertEquals("hello", requestData.body().getString("name"));
        assertEquals("test", requestData.getFilter().getString("x"));
        assertEquals(1, requestData.getPagination().getPage());
        assertEquals(20, requestData.getPagination().getPerPage());
        assertEquals("{\"headers\":{},\"body\":{\"name\":\"hello\"},\"filter\":{\"x\":\"test\"}," +
                     "\"pagination\":{\"page\":1,\"perPage\":20}}", requestData.toJson().encode());
    }

    @Test
    public void test_to_json_without_pagination() {
        RequestData requestData = RequestData.builder()
                                             .body(new JsonObject().put("name", "hello"))
                                             .filter(new JsonObject().put("x", "test"))
                                             .build();
        assertEquals("hello", requestData.body().getString("name"));
        assertEquals("test", requestData.getFilter().getString("x"));
        assertNull(requestData.getPagination());
        assertEquals("{\"headers\":{},\"body\":{\"name\":\"hello\"},\"filter\":{\"x\":\"test\"}}",
                     requestData.toJson().encode());
    }

    @Test
    public void test_from_json_1() {
        final JsonObject pagination = new JsonObject().put("page", 5).put("perPage", 10);
        final JsonObject data = new JsonObject().put("pagination", pagination)
                                                .put("body", new JsonObject())
                                                .put("filter", new JsonObject());
        final RequestData requestData = data.mapTo(RequestData.class);
        assertTrue(requestData.body().isEmpty());
        assertTrue(requestData.getFilter().isEmpty());
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
        RequestData requestData = JsonData.from(data, RequestData.class);
        assertEquals("xyz", requestData.body().getString("name"));
    }

}
