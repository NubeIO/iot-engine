package io.nubespark.controller;

import io.nubespark.JDBCService;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.RoutingContext;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.io.StringReader;
import java.util.Collections;

/**
 * Created by topsykretts on 4/26/18.
 */
public class RulesController {

    private final io.nubespark.reactivex.JDBCService jdbcService;
    private Logger logger = LoggerFactory.getLogger(RulesController.class);

    public RulesController(Vertx vertx) {
        jdbcService = JDBCService.createProxy(vertx, JDBCService.SERVICE_ADDRESS);
    }

    public Single<JsonObject> createRule(RoutingContext routingContext) {
        return Single.never();
    }

    public Single<JsonObject> getAll() {
        return Single.just("").map(s -> new JsonObject()
                .put("controller", "rules")
                .put("action", "getAll")
                .put("desc", "Get All Rules in system")
        );
    }

    public Single<JsonObject> getOne(String id) {
        String query = "SELECT * FROM metadata where " + id + " = ? ";
        return jdbcService.rxExecuteQueryWithParams(query, new JsonArray(Collections.singletonList("m:")))
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
                        jdbcService.rxExecuteQuery(query)
                                .map(message -> {
                                    JsonObject replyJson = new JsonObject()
                                            .put("controller", "rules")
                                            .put("action", "getFiloData")
                                            .put("desc", "Read data from filodb")
                                            .put("query", query);
                                    if (message != null) {
                                        replyJson.put("resultSet", message);
                                    }
                                    return replyJson;
                                })
                );
    }
}
