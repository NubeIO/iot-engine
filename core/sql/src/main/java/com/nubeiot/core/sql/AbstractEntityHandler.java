package com.nubeiot.core.sql;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;

public abstract class AbstractEntityHandler implements EntityHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final Vertx vertx;
    final Configuration jooqConfig;
    private String sharedKey = getClass().getName();

    public AbstractEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        this.jooqConfig = jooqConfig;
        this.vertx = vertx;
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }

    @Override
    public EventController eventClient() {
        return SharedDataDelegate.getEventController(vertx, sharedKey);
    }

    @Override
    public Path dataDir() {
        return SharedDataDelegate.getDataDir(vertx, sharedKey);
    }

    @Override
    public <D> D sharedData(String dataKey) {
        return SharedDataDelegate.getLocalDataValue(vertx, sharedKey, dataKey);
    }

    @Override
    public DSLContext dsl() {
        return jooqConfig.dsl();
    }

    @Override
    public <K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>> D dao(
        Class<D> daoClass) {
        Map<Class, Object> input = new LinkedHashMap<>();
        input.put(Configuration.class, jooqConfig);
        input.put(io.vertx.reactivex.core.Vertx.class, io.vertx.reactivex.core.Vertx.newInstance(vertx()));
        return ReflectionClass.createObject(daoClass, input);
    }

    @Override
    public JDBCRXGenericQueryExecutor genericQuery() {
        return new JDBCRXGenericQueryExecutor(jooqConfig, io.vertx.reactivex.core.Vertx.newInstance(vertx()));
    }

    @Override
    public ComplexQueryExecutor complexQuery() {
        return ComplexQueryExecutor.create(this);
    }

    protected boolean isNew(Table table) {
        if (jooqConfig.family() != SQLDialect.H2 && jooqConfig.family() != SQLDialect.POSTGRES) {
            return false;
        }
        String from = jooqConfig.family() == SQLDialect.H2 ? "information_schema.tables" : "pg_tables";
        String schema = jooqConfig.family() == SQLDialect.H2 ? "table_schema" : "schemaname";
        String tbl = jooqConfig.family() == SQLDialect.H2 ? "table_name" : "tablename";
        return 0 == dsl().selectCount()
                         .from(from)
                         .where(schema + " = '" + table.getSchema().getName() + "'")
                         .and(tbl + " = '" + table.getName() + "'")
                         .fetchOne(0, int.class);
    }

    //TODO HACK with H2 due to cannot generate `random_uuid()` from DDL. Must report to `jooq`
    protected int createDefaultUUID(@NonNull Map<Table, Field<UUID>> defField) {
        if (jooqConfig.family() == SQLDialect.H2) {
            return defField.entrySet()
                           .stream()
                           .map(entry -> dsl().execute("Alter table " + entry.getKey().getName() + " alter column " +
                                                       entry.getValue().getName() + " set default random_uuid()"))
                           .reduce(0, Integer::sum);
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    <T extends EntityHandler> T registerSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
        return (T) this;
    }

}
