package io.nubespark.hive;


import io.nubespark.hive.impl.HiveServiceImpl;
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

@ProxyGen
@VertxGen
public interface HiveService {

    String SERVICE_ADDRESS = "io.nubespark.hive.engine";

    String SERVICE_NAME = "hive-engine";

    @GenIgnore
    static Single<HiveService> create(Vertx vertx, JsonObject config) {
        return new HiveServiceImpl(vertx, config).initializeService();
    }

    @GenIgnore
    static io.nubespark.hive.reactivex.HiveService createProxy(Vertx vertx, String address) {
        return new io.nubespark.hive.reactivex.HiveService(new HiveServiceVertxEBProxy(vertx.getDelegate(), address));
    }

    @Fluent
    HiveService executeQueryWithParams(String query, JsonArray params, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    HiveService executeQuery(String query, Handler<AsyncResult<JsonObject>> resultHandler);
}
