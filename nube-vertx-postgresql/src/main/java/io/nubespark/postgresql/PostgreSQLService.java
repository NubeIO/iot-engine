package io.nubespark.postgresql;


import io.nubespark.postgresql.impl.PostgreSQLServiceImpl;
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
public interface PostgreSQLService {

    String SERVICE_ADDRESS = "io.nubespark.postgresql.engine";

    String SERVICE_NAME = "postgresql-engine";

    @GenIgnore
    static Single<PostgreSQLService> create(Vertx vertx, JsonObject config) {
        return new PostgreSQLServiceImpl(vertx, config).initializeService();
    }

    @GenIgnore
    static io.nubespark.postgresql.reactivex.PostgreSQLService createProxy(Vertx vertx, String address) {
        return new io.nubespark.postgresql.reactivex.PostgreSQLService(new PostgreSQLServiceVertxEBProxy(vertx.getDelegate(), address));
    }

    @Fluent
    PostgreSQLService executeQueryWithParams(String query, JsonArray params, JsonObject settings, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    PostgreSQLService executeQuery(String query, JsonObject settings, Handler<AsyncResult<JsonObject>> resultHandler);
}
