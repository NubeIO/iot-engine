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
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;

public interface EntityHandler {

    @SuppressWarnings("unchecked")
    static <POJO extends VertxPojo> POJO parse(@NonNull Class<POJO> modelClass, @NonNull JsonObject pojo) {
        return (POJO) ReflectionClass.createObject(modelClass).fromJson(pojo);
    }

    static <POJO extends VertxPojo> POJO parse(@NonNull Class<POJO> pojoClass, @NonNull Object data) {
        return parse(pojoClass, JsonData.tryParse(data).toJson());
    }

    Vertx vertx();

    //TODO Remove it
    default io.vertx.reactivex.core.Vertx getVertx() {
        return io.vertx.reactivex.core.Vertx.newInstance(vertx());
    }

    EventController eventClient();

    Path dataDir();

    <D> D sharedData(String dataKey);

    DSLContext dsl();

    <K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>> D dao(Class<D> daoClass);

    JDBCRXGenericQueryExecutor genericQuery();

    ComplexQueryExecutor complexQuery();

    /**
     * Check database is new or not. Normally just checking one specific table is existed or not.
     * <p>
     * Currently, it has not yet supported officially from {@code jooq}. So {@code NubeIO} supports 2 kinds: {@code H2}
     * and {@code PostgreSQL}. Other options, must be implemented by yourself.
     *
     * @return {@code true} if new database, else otherwise
     * @see <a href="https://github.com/jOOQ/jOOQ/issues/8038">https://github.com/jOOQ/jOOQ/issues/8038</a>
     */
    boolean isNew();

    /**
     * Init data in case of new database
     *
     * @return event message to know the process is success or not.
     * @see EventMessage
     */
    Single<EventMessage> initData();

    /**
     * Migrate data in case of existed database
     *
     * @return event message to know the process is success or not.
     * @see EventMessage
     */
    Single<EventMessage> migrate();

}
