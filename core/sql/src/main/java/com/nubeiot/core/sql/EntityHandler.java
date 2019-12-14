package com.nubeiot.core.sql;

import java.nio.file.Path;

import org.jooq.DSLContext;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;

/**
 * Represents for Entity handler.
 *
 * @since 1.0.0
 */
public interface EntityHandler {

    /**
     * Parse pojo.
     *
     * @param <P>        Type of {@code VertxPojo}
     * @param modelClass the model class
     * @param pojo       the pojo
     * @return the pojo
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    static <P extends VertxPojo> P parse(@NonNull Class<P> modelClass, @NonNull JsonObject pojo) {
        return (P) ReflectionClass.createObject(modelClass).fromJson(pojo);
    }

    /**
     * Parse pojo.
     *
     * @param <P>       Type of {@code VertxPojo}
     * @param pojoClass the pojo class
     * @param data      the data
     * @return the pojo
     * @since 1.0.0
     */
    static <P extends VertxPojo> P parse(@NonNull Class<P> pojoClass, @NonNull Object data) {
        return parse(pojoClass, JsonData.tryParse(data).toJson());
    }

    /**
     * Get Vertx.
     *
     * @return the vertx
     * @since 1.0.0
     */
    @NonNull Vertx vertx();

    /**
     * Get eventbus client.
     *
     * @return the eventbus client
     * @see EventbusClient
     * @since 1.0.0
     */
    @NonNull EventbusClient eventClient();

    /**
     * Data dir path.
     *
     * @return the path
     * @since 1.0.0
     */
    @NonNull Path dataDir();

    /**
     * Get shared data by {@code data key}.
     *
     * @param <D>     Type of {@code expectation result}
     * @param dataKey the data key
     * @return the result
     * @since 1.0.0
     */
    <D> D sharedData(String dataKey);

    /**
     * Add shared data.
     *
     * @param <D>     Type of {@code expectation result}
     * @param dataKey the data key
     * @param data    the data
     * @return the result
     * @since 1.0.0
     */
    <D> D addSharedData(String dataKey, D data);

    /**
     * Get {@code dsl context}.
     *
     * @return the dsl context
     * @see DSLContext
     * @since 1.0.0
     */
    @NonNull DSLContext dsl();

    /**
     * Create {@code DAO} by given {@code daoClass}.
     *
     * @param <K>      Type of {@code primary key}
     * @param <M>      Type of {@code VertxPojo}
     * @param <R>      Type of {@code UpdatableRecord}
     * @param <D>      Type of {@code VertxDAO}
     * @param daoClass the dao class
     * @return the instance of DAO
     * @since 1.0.0
     */
    <K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>> D dao(
        @NonNull Class<D> daoClass);

    /**
     * Get generic query executor.
     *
     * @return the generic query executor
     * @see JDBCRXGenericQueryExecutor
     * @since 1.0.0
     */
    @NonNull JDBCRXGenericQueryExecutor genericQuery();

    /**
     * Get complex query executor.
     *
     * @return the complex query executor
     * @see ComplexQueryExecutor
     * @since 1.0.0
     */
    @NonNull ComplexQueryExecutor complexQuery();

    /**
     * Execute any task before setup database
     *
     * @return single of reference to this, so the API can be used fluently
     * @since 1.0.0
     */
    @NonNull Single<EntityHandler> before();

    /**
     * Get {@code Schema handler}.
     *
     * @return the schema handler
     * @see SchemaHandler
     * @since 1.0.0
     */
    @NonNull SchemaHandler schemaHandler();

}
