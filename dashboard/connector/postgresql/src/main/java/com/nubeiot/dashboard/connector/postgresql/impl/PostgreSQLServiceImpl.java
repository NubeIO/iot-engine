package com.nubeiot.dashboard.connector.postgresql.impl;

import com.nubeiot.dashboard.connector.postgresql.PostgreSQLService;
import com.nubeiot.core.common.BaseService;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.asyncsql.PostgreSQLClient;
import io.vertx.reactivex.ext.sql.SQLClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PostgreSQLServiceImpl implements PostgreSQLService, BaseService {
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLService.class);
    private Vertx vertx;
    private JsonObject config;
    private Map<String, SQLClient> clients;

    public PostgreSQLServiceImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
    }

    @Override
    public Single<PostgreSQLService> initializeService() {
        logger.info("Initializing ...");
        clients = new HashMap<>();

        return Single.just(this);
    }

    private SingleObserver<JsonArray> toObserverFromArray(final Handler<AsyncResult<JsonObject>> handler) {
        final AtomicBoolean completed = new AtomicBoolean();
        return new SingleObserver<JsonArray>() {
            public void onSubscribe(@NonNull Disposable d) {
            }

            public void onSuccess(@NonNull JsonArray item) {
                if (completed.compareAndSet(false, true)) {
                    handler.handle(Future.succeededFuture(successMessage(item)));
                }
            }

            public void onError(Throwable error) {
                if (completed.compareAndSet(false, true)) {
                    handler.handle(Future.succeededFuture(failureMessage(error)));
                }
            }
        };
    }

    private Single<SQLConnection> getConnection(JsonObject settings) {
        SQLClient client;
        JsonObject pgConfig = new JsonObject(config.toString());
        if (!settings.toString().equals("{}")) {
            URL url = new URL(settings.getString("url"));
            pgConfig
                .put("host", url.getHost())
                .put("port", url.getPort())
                .put("database", url.getDatabase())
                .put("username", settings.getString("userName"))
                .put("password", settings.getString("password"));
        }

        String key = pgConfig.getString("host") + ":" + pgConfig.getInteger("port") + ":" + pgConfig.getString("username") + ":" +
            pgConfig.getString("password") + ":" + pgConfig.getString("database");

        if (clients.containsKey(key)) {
            client = clients.get(key);
        } else {
            client = PostgreSQLClient.createNonShared(vertx, pgConfig);
            clients.put(key, client);
        }

        return client.rxGetConnection()
            .flatMap(conn -> Single.just(conn)
                .doOnError(throwable -> logger.error("Cannot get connection object."))
                .doFinally(conn::close));
    }

    @Override
    public PostgreSQLService executeQueryWithParams(String sqlQuery, @Nullable JsonArray params, JsonObject settings, Handler<AsyncResult<JsonObject>> resultHandler) {
        executeQueryWithParams(sqlQuery, params, settings)
            .map(result -> new JsonArray(result.getNumRows() > 0 ? result.getRows() : Collections.emptyList()))
            .subscribe(toObserverFromArray(resultHandler));
        return this;
    }

    private Single<ResultSet> executeQueryWithParams(String sqlQuery, @Nullable JsonArray params, JsonObject settings) {
        return getConnection(settings)
            .flatMap(conn -> {
                if (params == null) {
                    return conn.rxQuery(sqlQuery);
                }
                return conn.rxQueryWithParams(sqlQuery, params);
            })
            .onErrorResumeNext(throwable -> {
                if (throwable.getMessage().contains("race -> false")) {
                    return Single.timer(100, TimeUnit.MILLISECONDS)
                        .flatMap(ignore -> executeQueryWithParams(sqlQuery, params, settings))
                        .map(result -> result);
                } else {
                    return Single.error(throwable);
                }
            })
            .map(result -> result);
    }

    @Override
    public PostgreSQLService executeQuery(String query, JsonObject settings, Handler<AsyncResult<JsonObject>> resultHandler) {
        return executeQueryWithParams(query, null, settings, resultHandler);
    }

    private JsonObject failureMessage(Throwable t) {
        return new JsonObject()
            .put("status", "FAILED")
            .put("error", t.getMessage());
    }

    private JsonObject successMessage(JsonArray jsonArray) {
        return new JsonObject()
            .put("status", "OK")
            .put("message", jsonArray);
    }

    private class URL {
        private String host;
        private int port = 5432;
        private String database = "test";

        URL(String url) {
            String[] values = url.split(":");
            host = values[0];
            if (values.length > 1) {
                String[] values$ = values[1].split("/");
                try {
                    port = Integer.parseInt(values$[0]);
                } catch (Exception ignored) {
                }

                if (values$.length > 1) {
                    database = values$[1];
                }
            }
        }

        String getHost() {
            return host;
        }

        int getPort() {
            return port;
        }

        String getDatabase() {
            return database;
        }
    }
}
