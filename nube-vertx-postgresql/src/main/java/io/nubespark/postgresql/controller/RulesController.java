package io.nubespark.postgresql.controller;

import io.nubespark.postgresql.PostgreSQLService;
import io.nubespark.utils.ErrorCodeException;
import io.nubespark.utils.ErrorCodes;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * Created by topsykretts on 4/26/18.
 */
public class RulesController {

    private final io.nubespark.postgresql.reactivex.PostgreSQLService postgreSQLService;

    public RulesController(Vertx vertx) {
        postgreSQLService = PostgreSQLService.createProxy(vertx, PostgreSQLService.SERVICE_ADDRESS);
    }

    public Single<JsonObject> getPostgreSQLData(String query, JsonObject settings) {
        return Single.just(query)
            .map(queryString -> {
                // Only handle select statements
                if (queryString.toUpperCase().trim().startsWith("SELECT")) {
                    return true;
                } else {
                    throw new ErrorCodeException(ErrorCodes.BAD_ACTION);
                }
            })
            .flatMap(ignored ->
                postgreSQLService.rxExecuteQuery(query, settings)
                    .map(message -> {
                        JsonObject replyJson = new JsonObject()
                            .put("action", "PostgreSQL Data")
                            .put("query", query);
                        if (message != null) {
                            replyJson.put("resultSet", message);
                        }
                        return replyJson;
                    })
            );
    }
}
