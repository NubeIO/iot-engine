package com.nubeiot.dashboard.connector.postgresql;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.github.zero.utils.Strings;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.asyncsql.PostgreSQLClient;
import io.vertx.reactivex.ext.sql.SQLClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.provider.RestNubeConfigProvider;

public class PostgreSqlRestController implements RestApi {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlRestController.class);
    private final Map<String, SQLClient> clients = new HashMap<>();

    @GET
    @Path("/info")
    public JsonObject info(@Context RoutingContext ctx) {
        return new JsonObject().put("name", "postgresql-engine-rest")
            .put("version", "1.0")
            .put("vert.x_version", "3.4.1")
            .put("java_version", "8.0");
    }

    @POST
    @Path("/engine")
    public Future<ResponseData> engine(@Context io.vertx.core.Vertx vertx, @Context RoutingContext ctx,
                                       @Context RestNubeConfigProvider config) {
        PostgreSqlConfig pgConfig = IConfig.from(config.getNubeConfig().getAppConfig(), PostgreSqlConfig.class);
        return postgreSqlQuery(new Vertx(vertx), pgConfig, ctx);
    }

    private Future<ResponseData> postgreSqlQuery(Vertx vertx, PostgreSqlConfig pgConfig, RoutingContext ctx) {
        Future<ResponseData> future = Future.future();
        // Check if we have a query in body
        JsonObject body = ctx.getBodyAsJson();
        String query = null;

        if (body != null) {
            query = body.getString("query", null);
        }

        if (query == null) {
            // Return query not specified error
            future.complete(new ResponseData().setStatus(HttpResponseStatus.BAD_REQUEST));
        } else {
            JsonObject settings = new JsonObject(
                Strings.getFirstNotNull(ctx.request().headers().get("Settings"), "{}"));
            final String finalQuery = query;
            executeQuery(vertx, settings, pgConfig, query)
                .subscribe(result -> future.complete(ResponseDataWriter.serializeResponseData(
                    messageWrapper(finalQuery, successMessage(result)).encode())), throwable -> future.complete(
                    ResponseDataWriter.serializeResponseData(
                        messageWrapper(finalQuery, failureMessage(throwable)).encode())
                                      .setStatus(HttpResponseStatus.BAD_REQUEST.code())));
        }
        return future;
    }

    private Single<ResultSet> executeQuery(Vertx vertx, JsonObject settings, PostgreSqlConfig pgConfig,
                                           String sqlQuery) {
        return getConnection(vertx, settings, pgConfig).flatMap(conn -> conn.rxQuery(sqlQuery))
            .onErrorResumeNext(throwable -> {
                // TODO: Find a better way of handling pooling
                if (throwable.getMessage().contains("race -> false")) {
                    return Single.timer(100, TimeUnit.MILLISECONDS)
                        .flatMap(ignore -> executeQuery(vertx, settings, pgConfig, sqlQuery))
                        .map(result -> result);
                } else {
                    return Single.error(throwable);
                }
            })
            .map(result -> result);
    }

    private Single<SQLConnection> getConnection(Vertx vertx, JsonObject settings, PostgreSqlConfig pgConfig) {
        SQLClient client;
        JsonObject pgConfigJson = pgConfig.toJson();
        // Example: Settings={"url": "localhost:5432/test", "userName": "postgres", "password": "123"}
        if (!settings.toString().equals("{}")) {
            PostgreSqlUrl url = new PostgreSqlUrl(settings.getString("url"));
            pgConfigJson.put("host", url.getHost())
                .put("port", url.getPort())
                .put("database", url.getDatabase())
                .put("username", settings.getString("userName"))
                .put("password", settings.getString("password"));
        }

        String key = pgConfigJson.getString("host") + ":" + pgConfigJson.getInteger("port") + ":" +
                     pgConfigJson.getString("username") + ":" + pgConfigJson.getString("password") + ":" +
                     pgConfigJson.getString("database");

        if (clients.containsKey(key)) {
            client = clients.get(key);
        } else {
            client = PostgreSQLClient.createNonShared(vertx, pgConfigJson);
            clients.put(key, client);
        }

        return client.rxGetConnection()
            .flatMap(conn -> Single.just(conn)
                .doOnError(throwable -> logger.error("Cannot get connection object."))
                .doFinally(conn::close));
    }

    private JsonObject messageWrapper(String query, JsonObject message) {
        return new JsonObject().put("action", "PostgreSql Data").put("query", query).put("resultSet", message);
    }

    private JsonObject successMessage(ResultSet result) {
        return new JsonObject().put("status", "OK")
            .put("message", new JsonArray(result.getNumRows() > 0 ? result.getRows() : Collections.emptyList()));
    }

    private JsonObject failureMessage(Throwable throwable) {
        return new JsonObject().put("status", "FAILED").put("message", throwable.getMessage());
    }

}
