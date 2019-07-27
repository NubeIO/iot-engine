package com.nubeiot.core.sql;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class EntityHandler {

    protected final Vertx vertx;
    @Getter(value = AccessLevel.PACKAGE)
    protected final Configuration jooqConfig;
    @Getter
    protected final JDBCRXGenericQueryExecutor queryExecutor;
    @Getter
    protected Function<String, Object> sharedDataFunc = k -> null;
    protected String sharedKey = "";

    public EntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        this.jooqConfig = jooqConfig;
        this.vertx = vertx;
        this.queryExecutor = new JDBCRXGenericQueryExecutor(jooqConfig, getVertx());
    }

    public <K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>> D getDao(
        Class<D> daoClass) {
        Map<Class, Object> input = new LinkedHashMap<>();
        input.put(Configuration.class, getJooqConfig());
        input.put(io.vertx.reactivex.core.Vertx.class, getVertx());
        return ReflectionClass.createObject(daoClass, input);
    }

    public io.vertx.reactivex.core.Vertx getVertx() {
        return io.vertx.reactivex.core.Vertx.newInstance(vertx);
    }

    /**
     * Check database is new or not. Normally just checking one specific table is existed or not.
     * <p>
     * Currently, it has not yet supported officially from {@code jooq}. So {@code NubeIO} supports 2 kinds: {@code H2}
     * and {@code POSTGRESQL}. Other options, must be implemented by yourself.
     *
     * @return {@code true} if new database, else otherwise
     * @see #isNew(Table)
     * @see <a href="https://github.com/jOOQ/jOOQ/issues/8038">https://github.com/jOOQ/jOOQ/issues/8038</a>
     */
    public abstract boolean isNew();

    /**
     * Init data in case of new database
     *
     * @return event message to know the process is success or not.
     * @see EventMessage
     */
    public abstract Single<EventMessage> initData();

    /**
     * Migrate data in case of existed database
     *
     * @return event message to know the process is success or not.
     * @see EventMessage
     */
    public abstract Single<EventMessage> migrate();

    protected boolean isNew(Table table) {
        if (jooqConfig.family() != SQLDialect.H2 && jooqConfig.family() != SQLDialect.POSTGRES) {
            return false;
        }
        String from = jooqConfig.family() == SQLDialect.H2 ? "information_schema.tables" : "pg_tables";
        String schema = jooqConfig.family() == SQLDialect.H2 ? "table_schema" : "schemaname";
        String tbl = jooqConfig.family() == SQLDialect.H2 ? "table_name" : "tablename";
        return 0 == jooqConfig.dsl()
                              .selectCount()
                              .from(from)
                              .where(schema + " = '" + table.getSchema().getName() + "'")
                              .and(tbl + " = '" + table.getName() + "'")
                              .fetchOne(0, int.class);
    }

    @SuppressWarnings("unchecked")
    <T extends EntityHandler> T registerSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
        this.sharedDataFunc = dataKey -> SharedDataDelegate.getLocalDataValue(vertx, sharedKey, dataKey);
        return (T) this;
    }

}
