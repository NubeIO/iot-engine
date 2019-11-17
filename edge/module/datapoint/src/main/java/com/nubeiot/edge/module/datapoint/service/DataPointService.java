package com.nubeiot.edge.module.datapoint.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.task.remote.ProtocolDispatcherTask;
import com.nubeiot.edge.module.datapoint.task.sync.SyncServiceFactory;
import com.nubeiot.edge.module.datapoint.task.sync.SyncTask;

import lombok.NonNull;

public interface DataPointService<P extends VertxPojo, M extends EntityMetadata>
    extends EntityService<P, M>, EventHttpService {

    static Set<? extends DataPointService> createServices(EntityHandler entityHandler) {
        final Map<Class, Object> inputs = Collections.singletonMap(EntityHandler.class, entityHandler);
        return ReflectionClass.stream(DataPointService.class.getPackage().getName(), DataPointService.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    //TODO refactor it
    static Set<EventMethodDefinition> definitionsForMany(@NonNull Collection<EventAction> availableEvents,
                                                         @NonNull EntityMetadata reference,
                                                         @NonNull EntityMetadata resource) {
        Map<EventAction, HttpMethod> crud = ActionMethodMapping.CRUD_MAP.get();
        ActionMethodMapping map = ActionMethodMapping.create(
            availableEvents.stream().filter(crud::containsKey).collect(Collectors.toMap(e -> e, crud::get)));
        final String servicePath = Urls.combinePath(
            Urls.capturePath(reference.singularKeyName(), reference.requestKeyName()), resource.singularKeyName());
        return Collections.singleton(EventMethodDefinition.create(servicePath, resource.requestKeyName(), map));
    }

    @Override
    default Optional<ProtocolDispatcherTask> taskBeforePersist() {
        return Optional.of(new ProtocolDispatcherTask(entityHandler()));
    }

    @Override
    default Optional<SyncTask> asyncTaskAfterPersist() {
        return SyncServiceFactory.get(entityHandler(), entityHandler().sharedData(DataPointIndex.DATA_SYNC_CFG));
    }

    default String api() {
        return "bios.datapoint." + this.getClass().getSimpleName();
    }

    default Set<EventMethodDefinition> definitions() {
        Map<EventAction, HttpMethod> crud = ActionMethodMapping.CRUD_MAP.get();
        ActionMethodMapping map = ActionMethodMapping.create(
            getAvailableEvents().stream().filter(crud::containsKey).collect(Collectors.toMap(e -> e, crud::get)));
        return Collections.singleton(EventMethodDefinition.create(servicePath(), context().requestKeyName(), map));
    }

    default String servicePath() {
        return Urls.toPathWithLC(context().singularKeyName());
    }

}
