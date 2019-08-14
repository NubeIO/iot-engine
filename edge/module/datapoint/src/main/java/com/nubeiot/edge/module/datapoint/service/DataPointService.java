package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.EntityService;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

public interface DataPointService<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>,
                                     M extends EntityMetadata<K, P, R, D>>
    extends EntityService<K, P, R, D, M>, EventHttpService {

    static Set<? extends DataPointService> createServices(EntityHandler entityHandler) {
        final Map<Class, Object> inputs = Collections.singletonMap(EntityHandler.class, entityHandler);
        return ReflectionClass.stream(DataPointService.class.getPackage().getName(), DataPointService.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    default boolean enableTimeAudit() {
        return true;
    }

    default String api() {
        return "datapoint." + this.getClass().getSimpleName();
    }

    default Set<EventMethodDefinition> definitions() {
        return Collections.singleton(
            EventMethodDefinition.createDefault(servicePath(), "/:" + metadata().requestKeyName()));
    }

    default String servicePath() {
        return "/" + Strings.toUrlPathWithLC(metadata().modelClass().getSimpleName());
    }

    Publisher publisher();

}
