package io.nubespark.postgresql.controller;

import io.nubespark.postgresql.PostgreSQLService;
import io.nubespark.utils.ErrorCodeException;
import io.nubespark.utils.ErrorCodes;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.io.StringReader;

/**
 * Created by topsykretts on 4/26/18.
 */
public class RulesController {

    private final io.nubespark.postgresql.reactivex.PostgreSQLService postgreSQLService;

    public RulesController(Vertx vertx) {
        postgreSQLService = PostgreSQLService.createProxy(vertx, PostgreSQLService.SERVICE_ADDRESS);
    }

    public Single<JsonObject> getPostgreSQLData(String query) {
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
                postgreSQLService.rxExecuteQuery(query)
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
