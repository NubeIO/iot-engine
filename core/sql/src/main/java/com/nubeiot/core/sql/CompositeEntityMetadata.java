package com.nubeiot.core.sql;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import lombok.NonNull;

public interface CompositeEntityMetadata<K, P extends VertxPojo, R extends UpdatableRecord<R>, DAO extends VertxDAO<R, P, K>, C extends CompositePojo<P>>
    extends EntityMetadata<K, P, R, DAO> {

    /**
     * Composite Pojo model class
     *
     * @return model class
     */
    @NonNull Class<C> compositeModelClass();

}
