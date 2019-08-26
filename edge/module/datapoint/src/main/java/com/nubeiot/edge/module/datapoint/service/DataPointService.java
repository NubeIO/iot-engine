package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
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

    default String api() {
        return "datapoint." + this.getClass().getSimpleName();
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
