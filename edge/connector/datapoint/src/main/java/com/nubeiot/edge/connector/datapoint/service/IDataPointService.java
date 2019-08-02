package com.nubeiot.edge.connector.datapoint.service;

import java.util.Collections;
import java.util.Map;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityService;

public interface IDataPointService<K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>>
    extends EntityService<K, M, R, D>, EventHttpService {

    default boolean enableTimeAudit() {
        return true;
    }

    default boolean enableFullResourceInCUDResponse() {
        return true;
    }

    default String address() {
        return this.getClass().getName();
    }

    default Map<String, EventMethodDefinition> definitions() {
        return Collections.singletonMap(address(), EventMethodDefinition.createDefault(
            "/" + modelClass().getSimpleName().toLowerCase(), "/:" + primaryKeyName()));
    }

}
