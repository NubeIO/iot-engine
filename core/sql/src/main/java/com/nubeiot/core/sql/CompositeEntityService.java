package com.nubeiot.core.sql;

import java.util.List;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

public interface CompositeEntityService<K, P extends VertxPojo, R extends UpdatableRecord<R>, DAO extends VertxDAO<R, P, K>, M extends EntityMetadata<K, P, R, DAO>>
    extends EntityService<K, P, R, DAO, M> {

    List<EntityService> children();

}
