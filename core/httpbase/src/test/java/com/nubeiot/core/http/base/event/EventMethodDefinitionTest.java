package com.nubeiot.core.http.base.event;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NotFoundException;

public class EventMethodDefinitionTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_wrong_argument() {
        EventMethodDefinition.createDefault("", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_wrong_multiParam_pattern() {
        EventMethodDefinition.createDefault("/client/:clientId/product/:productId",
                                            "/client/:clientId/product/:productId");
    }

    @Test(expected = NotFoundException.class)
    public void test_not_found() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/abc", "/abc/:id");
        definition.search("/abcd", HttpMethod.GET);
    }

    @Test(expected = NotFoundException.class)
    public void test_not_found_multiParam_pattern() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/abc/:cid/prod", "/abc/:cid/prod/:pid");
        definition.search("/abc/222/xyz/prod", HttpMethod.GET);
    }

    @Test
    public void test_search() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/abc", "/abc/:id");
        Assert.assertEquals(EventAction.GET_LIST, definition.search("/abc", HttpMethod.GET));
        Assert.assertEquals(EventAction.GET_ONE, definition.search("/abc/xyz", HttpMethod.GET));
        Assert.assertEquals(EventAction.CREATE, definition.search("/abc", HttpMethod.POST));
        Assert.assertEquals(EventAction.UPDATE, definition.search("/abc/xyz", HttpMethod.PUT));
        Assert.assertEquals(EventAction.PATCH, definition.search("/abc/xyz", HttpMethod.PATCH));
        Assert.assertEquals(EventAction.REMOVE, definition.search("/abc/xyz", HttpMethod.DELETE));
    }

    @Test
    public void test_search_multiParam_pattern_has_resource_between() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/client/:clientId/product",
                                                                               "/client/:clientId/product/:productId");
        Assert.assertEquals(EventAction.GET_LIST, definition.search("/client/123/product", HttpMethod.GET));
        Assert.assertEquals(EventAction.GET_ONE, definition.search("/client/123/product/456", HttpMethod.GET));
        Assert.assertEquals(EventAction.CREATE, definition.search("/client/123/product", HttpMethod.POST));
        Assert.assertEquals(EventAction.UPDATE, definition.search("/client/123/product/456", HttpMethod.PUT));
        Assert.assertEquals(EventAction.PATCH, definition.search("/client/123/product/456", HttpMethod.PATCH));
        Assert.assertEquals(EventAction.REMOVE, definition.search("/client/123/product/456", HttpMethod.DELETE));
    }

    @Test
    public void test_search_multiParam_pattern_no_resource_between() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/client/:clientId/",
                                                                               "/client/:clientId/:productId");
        Assert.assertEquals(EventAction.GET_LIST, definition.search("/client/123/", HttpMethod.GET));
        Assert.assertEquals(EventAction.GET_ONE, definition.search("/client/123/456", HttpMethod.GET));
        Assert.assertEquals(EventAction.CREATE, definition.search("/client/123/", HttpMethod.POST));
        Assert.assertEquals(EventAction.UPDATE, definition.search("/client/123/456", HttpMethod.PUT));
        Assert.assertEquals(EventAction.PATCH, definition.search("/client/123/456", HttpMethod.PATCH));
        Assert.assertEquals(EventAction.REMOVE, definition.search("/client/123/456", HttpMethod.DELETE));
    }

    @Test
    public void test_to_json() throws JSONException {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/abc", "/abc/:id");
        System.out.println(definition.toJson());
        JSONAssert.assertEquals("{\"servicePath\":\"/abc\",\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\"}," +
                                "{\"action\":\"CREATE\",\"method\":\"POST\"},{\"action\":\"UPDATE\"," +
                                "\"method\":\"PUT\",\"capturePath\":\"/abc/:id\",\"regexPath\":\"/abc/.+\"}," +
                                "{\"action\":\"GET_ONE\",\"method\":\"GET\",\"capturePath\":\"/abc/:id\"," +
                                "\"regexPath\":\"/abc/.+\"},{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
                                "\"capturePath\":\"/abc/:id\",\"regexPath\":\"/abc/.+\"},{\"action\":\"REMOVE\"," +
                                "\"method\":\"DELETE\",\"capturePath\":\"/abc/:id\",\"regexPath\":\"/abc/.+\"}]}\n",
                                definition.toJson().encode(), JSONCompareMode.LENIENT);
    }

    @Test
    public void test_to_json_multiParams() throws JSONException {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/c/:cId/p", "/c/:cId/p/:pId");
        System.out.println(definition.toJson());
        JSONAssert.assertEquals("{\"servicePath\":\"/c/[^/]+/p\",\"mapping\":[{\"action\":\"GET_LIST\"," +
                                "\"method\":\"GET\",\"capturePath\":\"/c/:cId/p\",\"regexPath\":\"/c/[^/]+/p\"}," +
                                "{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath\":\"/c/:cId/p\"," +
                                "\"regexPath\":\"/c/[^/]+/p\"},{\"action\":\"UPDATE\",\"method\":\"PUT\"," +
                                "\"capturePath\":\"/c/:cId/p/:pId\",\"regexPath\":\"/c/[^/]+/p/.+\"}," +
                                "{\"action\":\"GET_ONE\",\"method\":\"GET\",\"capturePath\":\"/c/:cId/p/:pId\"," +
                                "\"regexPath\":\"/c/[^/]+/p/.+\"},{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
                                "\"capturePath\":\"/c/:cId/p/:pId\",\"regexPath\":\"/c/[^/]+/p/.+\"}," +
                                "{\"action\":\"REMOVE\",\"method\":\"DELETE\",\"capturePath\":\"/c/:cId/p/:pId\"," +
                                "\"regexPath\":\"/c/[^/]+/p/.+\"}]}",
                                definition.toJson().encode(), JSONCompareMode.LENIENT);
    }

    @Test
    public void test_from_json() {
        EventMethodDefinition definition = JsonData.from(
            "{\"servicePath\":\"/abc\",\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\"}," +
            "{\"action\":\"CREATE\",\"method\":\"POST\"},{\"action\":\"UPDATE\",\"method\":\"PUT\"," +
            "\"capturePath\":\"/abc/:id\"},{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"capturePath\":\"/abc/:id\"},{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
            "\"capturePath\":\"/abc/:id\"},{\"action\":\"REMOVE\",\"method\":\"DELETE\"," +
            "\"capturePath\":\"/abc/:id\"}]}", EventMethodDefinition.class);
        System.out.println(definition.toJson());
        Assert.assertEquals(EventAction.GET_LIST, definition.search("/abc", HttpMethod.GET));
        Assert.assertEquals(EventAction.GET_ONE, definition.search("/abc/xyz", HttpMethod.GET));
        Assert.assertEquals(EventAction.CREATE, definition.search("/abc", HttpMethod.POST));
        Assert.assertEquals(EventAction.UPDATE, definition.search("/abc/xyz", HttpMethod.PUT));
        Assert.assertEquals(EventAction.PATCH, definition.search("/abc/xyz", HttpMethod.PATCH));
        Assert.assertEquals(EventAction.REMOVE, definition.search("/abc/xyz", HttpMethod.DELETE));
    }

}
