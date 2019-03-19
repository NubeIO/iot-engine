package com.nubeiot.core.http.base.event;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;

public class EventMethodDefinitionTest {

    @Test(expected = IllegalArgumentException.class)
    public void test_wrong_argument() {
        EventMethodDefinition.createDefault("", "");
    }

    @Test
    public void test_search() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/abc", "/abc/.+");
        Assert.assertEquals(EventAction.GET_LIST, definition.search("/abc", HttpMethod.GET));
        Assert.assertEquals(EventAction.GET_ONE, definition.search("/abc/xyz", HttpMethod.GET));
        Assert.assertEquals(EventAction.CREATE, definition.search("/abc", HttpMethod.POST));
        Assert.assertEquals(EventAction.UPDATE, definition.search("/abc/xyz", HttpMethod.PUT));
        Assert.assertEquals(EventAction.PATCH, definition.search("/abc/xyz", HttpMethod.PATCH));
        Assert.assertEquals(EventAction.REMOVE, definition.search("/abc/xyz", HttpMethod.DELETE));
    }

    @Test
    public void test_to_json() throws JSONException {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/abc", "/abc/.+");
        System.out.println(definition.toJson());
        JSONAssert.assertEquals("{\"eventPaths\":{\"/abc\":[{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
                                "\"regexPath\":\"/abc/.+\"},{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
                                "\"regexPath\":\"/abc/.+\"},{\"action\":\"GET_LIST\",\"method\":\"GET\"}," +
                                "{\"action\":\"UPDATE\",\"method\":\"PUT\",\"regexPath\":\"/abc/.+\"}," +
                                "{\"action\":\"CREATE\",\"method\":\"POST\"},{\"action\":\"REMOVE\"," +
                                "\"method\":\"DELETE\",\"regexPath\":\"/abc/.+\"}]}}", definition.toJson().encode(),
                                JSONCompareMode.LENIENT);
    }

    @Test
    public void test_from_json() {
        EventMethodDefinition definition = JsonData.from(
            "{\"eventPaths\":{\"/abc\":[{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
            "\"regexPath\":\"/abc/.+\"},{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"regexPath\":\"/abc/.+\"},{\"action\":\"GET_LIST\",\"method\":\"GET\"}," +
            "{\"action\":\"UPDATE\",\"method\":\"PUT\",\"regexPath\":\"/abc/.+\"}," +
            "{\"action\":\"CREATE\",\"method\":\"POST\"},{\"action\":\"REMOVE\"," +
            "\"method\":\"DELETE\",\"regexPath\":\"/abc/.+\"}]}}", EventMethodDefinition.class);
        Assert.assertEquals(EventAction.GET_LIST, definition.search("/abc", HttpMethod.GET));
        Assert.assertEquals(EventAction.GET_ONE, definition.search("/abc/xyz", HttpMethod.GET));
        Assert.assertEquals(EventAction.CREATE, definition.search("/abc", HttpMethod.POST));
        Assert.assertEquals(EventAction.UPDATE, definition.search("/abc/xyz", HttpMethod.PUT));
        Assert.assertEquals(EventAction.PATCH, definition.search("/abc/xyz", HttpMethod.PATCH));
        Assert.assertEquals(EventAction.REMOVE, definition.search("/abc/xyz", HttpMethod.DELETE));
    }

}
