package com.nubeiot.dashboard.connector.hive;

import static com.nubeiot.core.http.handler.ResponseDataWriter.responseData;

import java.util.Collections;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.RestConfigProvider;
import com.nubeiot.core.http.rest.RestApi;

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
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

public class HiveRestController implements RestApi {

    private static final Logger logger = LoggerFactory.getLogger(HiveRestController.class);

    @GET
    @Path("/info")
    public JsonObject info(@Context RoutingContext ctx) {
        return new JsonObject().put("name", "hive-engine-rest")
            .put("version", "1.0")
            .put("vert.x_version", "3.4.1")
            .put("java_version", "8.0");
    }

    @POST
    @Path("/engine")
    public Future<ResponseData> engine(@Context io.vertx.core.Vertx vertx, @Context RoutingContext ctx,
                                       @Context RestConfigProvider config) {
        HiveConfig hiveConfig = IConfig.from(config.getConfig().getAppConfig(), HiveConfig.class);
        return hiveQuery(new Vertx(vertx), hiveConfig, ctx);
    }

    private Future<ResponseData> hiveQuery(Vertx vertx, HiveConfig hiveConfig, RoutingContext ctx) {
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
        } else if (!query.toUpperCase().trim().startsWith("SELECT")) {
            future.complete(new ResponseData().setStatus(HttpResponseStatus.UNAUTHORIZED));
        } else {
            final String finalQuery = query;
            executeQuery(vertx, hiveConfig, query)
                .subscribe(
                    result -> future.complete(responseData(messageWrapper(finalQuery, successMessage(result)).encode())
                                                  .setStatus(HttpResponseStatus.OK)),
                    error -> future.complete(responseData(messageWrapper(finalQuery, failureMessage(error)).encode())
                                                 .setStatus(HttpResponseStatus.BAD_REQUEST)));
        }
        return future;
    }

    private Single<ResultSet> executeQuery(Vertx vertx, HiveConfig hiveConfig, String sqlQuery) {
        return getConnection(vertx, hiveConfig).flatMap(conn -> conn.rxQuery(sqlQuery));
    }

    private Single<SQLConnection> getConnection(Vertx vertx, HiveConfig hiveConfig) {
        JDBCClient client = JDBCClient.createNonShared(vertx, hiveConfig.toJson());

        return client.rxGetConnection()
            .flatMap(conn -> Single.just(conn)
                .doOnError(throwable -> logger.error("Cannot get connection object."))
                .doFinally(conn::close));
    }

    private JsonObject messageWrapper(String query, JsonObject message) {
        return new JsonObject().put("action", "FiloDB Data").put("query", query).put("resultSet", message);
    }

    private JsonObject successMessage(ResultSet result) {
        return new JsonObject().put("status", "OK")
            .put("message", new JsonArray(
                result.getNumRows() > 0 ? result.getRows() : Collections.emptyList()));
    }

    private JsonObject failureMessage(Throwable throwable) {
        return new JsonObject().put("status", "FAILED").put("message", throwable.getMessage());
    }

}
