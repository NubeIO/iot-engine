package com.nubeiot.dashboard.connector.hive;

import io.reactivex.Single;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.dashboard.connector.hive.impl.HiveServiceImpl;

@ProxyGen
@VertxGen
public interface HiveService {

    String SERVICE_ADDRESS = "com.nubeiot.dashboard.connector.hive";

    String SERVICE_NAME = "hive-engine";

    @GenIgnore
    static Single<HiveService> create(Vertx vertx, JsonObject config) {
        return new HiveServiceImpl(vertx, config).initializeService();
    }

    @GenIgnore
    static com.nubeiot.dashboard.connector.hive.reactivex.HiveService createProxy(Vertx vertx, String address) {
        return new com.nubeiot.dashboard.connector.hive.reactivex.HiveService(
                new HiveServiceVertxEBProxy(vertx.getDelegate(), address));
    }

    @Fluent
    HiveService executeQueryWithParams(String query, JsonArray params, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    HiveService executeQuery(String query, Handler<AsyncResult<JsonObject>> resultHandler);

}
