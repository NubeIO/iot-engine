package com.nubeiot.dashboard.utils;

import java.util.List;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.common.utils.HttpException;

public class MongoUtils {
    public static JsonObject idQuery(String _id) {
        return new JsonObject().put("_id", _id);
    }

    public static Single<String> postDocument(MongoClient mongoClient, String collection, JsonObject document) {
        return mongoClient.rxFindOne(collection, new JsonObject().put("_id", document.getString("_id", "")), null)
            .flatMap(response -> {
                if (response == null) {
                    return mongoClient.rxSave(collection, document);
                } else {
                    throw new HttpException(HttpResponseStatus.CONFLICT, "Same value existence on our Database.");
                }
            });
    }

    public static JsonObject pickOneOrNullJsonObject(List<JsonObject> jsonObjectList) {
        if (jsonObjectList.size() > 0) {
            return jsonObjectList.get(0);
        } else {
            return new JsonObject();
        }
    }
}
