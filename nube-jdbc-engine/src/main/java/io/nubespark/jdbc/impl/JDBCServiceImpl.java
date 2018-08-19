package io.nubespark.jdbc.impl;

import io.nubespark.jdbc.JDBCService;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;


public class JDBCServiceImpl implements JDBCService {
    // SQL statement
    private static final String CREATE_DEMO_STATEMENT = "CREATE TABLE IF NOT EXISTS metadata (id int, name varchar(100), tag varchar" +
            "(100))";
    private static final String GET_FIRST_DEMO = "SELECT * FROM metadata LIMIT 1";
    private static final String INSERT_DEMO = "INSERT into metadata (id, name, tag) VALUES ";


    private final Logger logger = LoggerFactory.getLogger(JDBCServiceImpl.class);
    private final JDBCClient client;
    private Map<String, String> tagsMap = new HashMap<String, String>() {{
        put("Boiler 1", "boiler");
        put("Boiler 2", "boiler");
        put("Chiller 1", "chiller");
        put("Chiller 2", "chiller");
        put("AHU 1", "ahu");
        put("AHU 2", "ahu");
        put("Set Point1", "sp");
    }};

    public JDBCServiceImpl(Vertx vertx, JsonObject config, Handler<AsyncResult<JDBCService>> resultHandler) {
        this.client = JDBCClient.createNonShared(vertx, config);

        logger.info("Initializing tags database...");

//        initializeDatabase(resultHandler);
    }

    public SingleObserver<JsonObject> toObserverFromObject(final Handler<AsyncResult<JsonObject>> handler) {
        final AtomicBoolean completed = new AtomicBoolean();
        return new SingleObserver<JsonObject>() {
            public void onSubscribe(@NonNull Disposable d) {
            }

            public void onSuccess(@NonNull JsonObject item) {
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


    // todo Fix initialization query
    private void initializeDatabase(Handler<AsyncResult<JDBCService>> resultHandler) {
        getConnection()
                // Create demo table if necessary
                .flatMap(conn -> conn.rxExecute(CREATE_DEMO_STATEMENT)
                        .doOnError(throwable -> logger.error("Failed to execute query: " + CREATE_DEMO_STATEMENT + throwable.getMessage()))
                        .andThen(Single.just(conn))
                )

                .flatMap(conn -> conn.rxQuery(GET_FIRST_DEMO) // Check if there are any records present
                        .filter(resultSet -> resultSet.getNumRows() == 0)
                        .flatMap((Function<ResultSet, MaybeSource<?>>) resultSet -> {
                            logger.info("Creating tags Database...");
                            JsonArray params = new JsonArray();
                            StringJoiner queries = new StringJoiner(" , ");
                            int i = 0;
                            for (Map.Entry<String, String> entry : tagsMap.entrySet()) {
                                params.add(i++);
                                params.add(entry.getKey());
                                params.add(entry.getValue());
                                queries.add("(?,?,?)");
                            }
                            String sqlQuery = INSERT_DEMO + queries.toString();
                            return conn.rxUpdateWithParams(sqlQuery, params)
                                    .doOnSuccess(updateResult -> logger.info("Tags database initialized..."))
                                    .doOnError(throwable -> logger.error("Failed to insert records: " + throwable.getMessage()))
                                    .toMaybe();
                        })
                        .toSingle())
                .map(conn -> this)
                .subscribe(SingleHelper.toObserver(resultHandler));
    }

    private Single<SQLConnection> getConnection() {
        return client.rxGetConnection()
                .flatMap(conn -> Single.just(conn)
                        .doOnError(throwable -> logger.error("Cannot get connection object."))
                        .doFinally(conn::close));
    }

    @Override
    public JDBCService executeQueryWithParams(String sqlQuery, @Nullable JsonArray params, Handler<AsyncResult<JsonObject>> resultHandler) {
        getConnection()
                .flatMap(conn -> {
                    if (params == null) {
                        return conn.rxQuery(sqlQuery);
                    }
                    return conn.rxQueryWithParams(sqlQuery, params);
                })
                .map(result -> new JsonArray(result.getNumRows() > 0 ? result.getRows() : Collections.emptyList()))
                .subscribe(toObserverFromArray(resultHandler));

        return this;
    }

    @Override
    public JDBCService executeQuery(String query, Handler<AsyncResult<JsonObject>> resultHandler) {
        return executeQueryWithParams(query, null, resultHandler);
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

    private JsonObject successMessage(JsonObject object) {
        return new JsonObject()
                .put("status", "OK")
                .put("message", object);

    }

}
