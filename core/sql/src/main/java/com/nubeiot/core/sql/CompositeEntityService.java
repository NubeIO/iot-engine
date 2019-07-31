package com.nubeiot.core.sql;

import java.util.List;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

public interface CompositeEntityService<K, M extends VertxPojo, R extends UpdatableRecord<R>, DAO extends VertxDAO<R, M, K>>
    extends EntityService<K, M, R, DAO> {

    List<EntityService> children();

}
