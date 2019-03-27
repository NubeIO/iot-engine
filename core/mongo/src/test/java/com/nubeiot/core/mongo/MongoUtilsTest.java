package com.nubeiot.core.mongo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MongoUtilsTest {

    @Test
    public void getIds() {
        List<JsonObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(new JsonObject().put("_id", "1"));
        jsonObjectList.add(new JsonObject().put("_id", "2"));
        String[] _ids = MongoUtils.getIds(jsonObjectList);
        Assert.assertEquals(jsonObjectList.size(), _ids.length);
        Assert.assertEquals(_ids[0], "1");
    }

    @Test
    public void getIdsOnList() {
        List<JsonObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(new JsonObject().put("_id", "1"));
        jsonObjectList.add(new JsonObject().put("_id", "2"));
        List<String> _ids = MongoUtils.getIdsOnList(jsonObjectList);
        Assert.assertEquals(jsonObjectList.size(), _ids.size());
        Assert.assertEquals(_ids.get(0), "1");
    }

    @Test
    public void getIdsOnJsonArray() {
        List<JsonObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(new JsonObject().put("_id", "1"));
        jsonObjectList.add(new JsonObject().put("_id", "2"));
        JsonArray _ids = MongoUtils.getIdsOnJsonArray(jsonObjectList);
        Assert.assertEquals(jsonObjectList.size(), _ids.size());
        Assert.assertEquals(_ids.getString(0), "1");
    }

    @Test
    public void getMatchValueOrFirstOneHappyCase() {
        List<JsonObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(new JsonObject().put("_id", "1").put("first_name", "Shane"));
        jsonObjectList.add(new JsonObject().put("_id", "2").put("first_name", "Smith"));
        JsonObject jsonObject = MongoUtils.getMatchValueOrFirstOne(jsonObjectList, "1");
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals(jsonObject.getString("first_name"), "Shane");
    }

    @Test
    public void getMatchValueOrFirstOneNull() {
        List<JsonObject> jsonObjectList = new ArrayList<>();
        JsonObject jsonObject = MongoUtils.getMatchValueOrFirstOne(jsonObjectList, "1");
        Assert.assertNull(jsonObject);
    }

}
