package com.nubeiot.core.mongo;

import java.util.ArrayList;
import java.util.List;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.exceptions.HttpException;

public class MongoUtils {

    public static JsonObject idQuery(String _id) {
        return new JsonObject().put("_id", _id);
    }

    public static Single<String> postDocument(MongoClient mongoClient, String collection, JsonObject document) {
        return mongoClient
            .rxFindOne(collection, new JsonObject().put("_id", document.getString("_id", "")), null)
            .flatMap(response -> {
                if (response == null) {
                    return mongoClient.rxSave(collection, document);
                } else {
                    throw new HttpException(HttpResponseStatus.CONFLICT,
                                            "Same value existence on our Database.");
                }
            });
    }

    public static String[] getIds(List<JsonObject> jsonObjectList) {
        List<String> _ids = new ArrayList<>();
        for (Object object : jsonObjectList) {
            JsonObject jsonObject = (JsonObject) object;
            _ids.add(jsonObject.getString("_id"));
        }
        return _ids.toArray(new String[0]);
    }

    public static List<String> getIdsOnList(List<JsonObject> jsonObjectList) {
        List<String> _ids = new ArrayList<>();
        for (JsonObject jsonObject : jsonObjectList) {
            _ids.add(jsonObject.getString("_id"));
        }
        return _ids;
    }

    public static JsonArray getIdsOnJsonArray(List<JsonObject> jsonObjectList) {
        JsonArray _ids = new JsonArray();
        for (JsonObject jsonObject : jsonObjectList) {
            _ids.add(jsonObject.getString("_id"));
        }
        return _ids;
    }

    public static JsonObject getMatchValueOrFirstOne(List<JsonObject> jsonObjectList, String _id) {
        for (Object object : jsonObjectList) {
            JsonObject jsonObject = (JsonObject) object;
            if (jsonObject.getString("_id").equals(_id)) {
                return jsonObject;
            }
        }
        return jsonObjectList.size() > 0 ? jsonObjectList.get(0) : new JsonObject();
    }

}
