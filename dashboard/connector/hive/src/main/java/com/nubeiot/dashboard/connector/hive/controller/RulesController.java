package com.nubeiot.dashboard.connector.hive.controller;

import com.nubeiot.dashboard.connector.hive.HiveService;
import com.nubeiot.core.common.utils.ErrorCodeException;
import com.nubeiot.core.common.utils.ErrorCodes;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.io.StringReader;
import java.util.Collections;

/**
 * Created by topsykretts on 4/26/18.
 */
public class RulesController {

    private final com.nubeiot.dashboard.connector.hive.reactivex.HiveService hiveService;

    public RulesController(Vertx vertx) {
        hiveService = HiveService.createProxy(vertx, HiveService.SERVICE_ADDRESS);
    }

    public Single<JsonObject> getOne(String id) {
        String query = "SELECT * FROM metadata where " + id + " = ? ";
        return hiveService.rxExecuteQueryWithParams(query, new JsonArray(Collections.singletonList("m:")))
            .map(message -> {
                JsonObject replyJson = new JsonObject()
                    .put("controller", "rules")
                    .put("action", "getOne")
                    .put("desc", "Get singe rule for given param id")
                    .put("id", id);
                if (message != null) {
                    replyJson.put("reply", message);
                }
                return replyJson;
            });

    }

    public Single<JsonObject> getFiloData(String query) {
        return Single.just(query)
            .map(queryString -> {
                CCJSqlParserManager ccjSqlParserManager = new CCJSqlParserManager();
                final Statement statement = ccjSqlParserManager.parse(new StringReader(query));
                // Only handle select statements
                if (!(statement instanceof Select)) {
                    throw new ErrorCodeException(ErrorCodes.BAD_ACTION);
                } else {
                    return statement;
                }
            })
            .flatMap(ignored ->
                hiveService.rxExecuteQuery(query)
                    .map(message -> {
                        JsonObject replyJson = new JsonObject()
                            .put("action", "FiloDB Data")
                            .put("query", query);
                        if (message != null) {
                            replyJson.put("resultSet", message);
                        }
                        return replyJson;
                    })
            );
    }
}
