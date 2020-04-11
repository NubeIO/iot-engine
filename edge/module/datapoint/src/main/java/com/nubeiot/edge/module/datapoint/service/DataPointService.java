package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.zero.utils.Reflections.ReflectionClass;
import io.github.zero.utils.Urls;

import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.core.sql.workflow.task.EntityTask;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.task.remote.ProtocolDispatcherTask;
import com.nubeiot.edge.module.datapoint.task.sync.SyncServiceFactory;

import lombok.NonNull;

public interface DataPointService<P extends VertxPojo, M extends EntityMetadata>
    extends EntityService<P, M>, EventHttpService {

    static Set<DataPointService> createServices(@NonNull EntityHandler entityHandler) {
        final Map<Class, Object> inputs = Collections.singletonMap(EntityHandler.class, entityHandler);
        return ReflectionClass.stream(DataPointService.class.getPackage().getName(), DataPointService.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    @Override
    default EntityTask prePersistTask() {
        return new ProtocolDispatcherTask(entityHandler());
    }

    @Override
    default EntityTask postPersistAsyncTask() {
        return SyncServiceFactory.get(entityHandler(), entityHandler().sharedData(DataPointIndex.DATA_SYNC_CFG));
    }

    default String api() {
        return DataPointApiService.DEFAULT.lookupApiName(context());
    }

    default Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(getAvailableEvents(), this::servicePath, context()::requestKeyName);
    }

    default String servicePath() {
        return Urls.toPathWithLC(context().singularKeyName());
    }

}
