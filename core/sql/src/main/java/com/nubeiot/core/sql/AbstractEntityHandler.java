package com.nubeiot.core.sql;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents for Abstract entity handler.
 *
 * @since 1.0.0
 */
public abstract class AbstractEntityHandler implements EntityHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @NonNull
    final Configuration jooqConfig;
    @NonNull
    private final Vertx vertx;
    @Getter(value = AccessLevel.PROTECTED)
    private String sharedKey = getClass().getName();

    /**
     * Instantiates a new Abstract entity handler.
     *
     * @param jooqConfig the jooq config
     * @param vertx      the vertx
     * @since 1.0.0
     */
    public AbstractEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        this.jooqConfig = jooqConfig;
        this.vertx = vertx;
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }

    @Override
    public EventbusClient eventClient() {
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
    public <D> D addSharedData(String dataKey, D data) {
        return SharedDataDelegate.addLocalDataValue(vertx, sharedKey, dataKey, data);
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

    @Override
    public Single<EntityHandler> before() {
        return Single.just(this);
    }

    @SuppressWarnings("unchecked")
    <T extends EntityHandler> T registerSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
        return (T) this;
    }

}
