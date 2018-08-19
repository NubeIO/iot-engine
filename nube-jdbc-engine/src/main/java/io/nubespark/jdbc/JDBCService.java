package io.nubespark.jdbc;


import io.nubespark.jdbc.impl.JDBCServiceImpl;
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
public interface JDBCService {

    String SERVICE_ADDRESS = "io.nubespark.jdbc.engine";

    String SERVICE_NAME = "jdbc-engine";

    @GenIgnore
    static JDBCService create(Vertx vertx, JsonObject config, Handler<AsyncResult<JDBCService>> resultHandler) {
        return new JDBCServiceImpl(vertx, config, resultHandler);
    }

    @GenIgnore
    static io.nubespark.jdbc.reactivex.JDBCService createProxy(Vertx vertx, String address) {
        return new io.nubespark.jdbc.reactivex.JDBCService(new JDBCServiceVertxEBProxy(vertx.getDelegate(), address));
    }

    @Fluent
    JDBCService executeQueryWithParams(String query, JsonArray params, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    JDBCService executeQuery(String query, Handler<AsyncResult<JsonObject>> resultHandler);
}
