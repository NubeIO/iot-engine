package io.nubespark.utils;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;


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
}
