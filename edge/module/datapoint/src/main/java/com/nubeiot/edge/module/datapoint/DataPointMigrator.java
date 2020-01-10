package com.nubeiot.edge.module.datapoint;

import java.util.Optional;

import org.jooq.Catalog;
import org.jooq.impl.DSL;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.SchemaMigrator;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeMetadata;
import com.nubeiot.edge.module.datapoint.cache.DataCacheInitializer;

import lombok.NonNull;

final class DataPointMigrator implements SchemaMigrator {

    @Override
    public Single<EventMessage> execute(@NonNull EntityHandler entityHandler, @NonNull Catalog catalog) {
        final DataPointEntityHandler handler = (DataPointEntityHandler) entityHandler;
        return handler.dao(EdgeMetadata.INSTANCE)
                      .findOneByCondition(DSL.trueCondition())
                      .filter(Optional::isPresent)
                      .map(Optional::get)
                      .map(handler::cacheEdge)
                      .map(edge -> new JsonObject())
                      .switchIfEmpty(handler.initDataFromConfig(EventAction.MIGRATE))
                      .map(json -> EventMessage.success(EventAction.MIGRATE, json))
                      .doOnSuccess(ignore -> new DataCacheInitializer().init(handler));
    }

}
