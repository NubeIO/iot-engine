package com.nubeiot.edge.connector.datapoint.service;

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
import com.nubeiot.core.sql.EntityService;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

public interface DataPointService<K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>>
    extends EntityService<K, M, R, D>, EventHttpService {

    static Set<? extends DataPointService> createServices(EntityHandler entityHandler) {
        final Map<Class, Object> inputs = Collections.singletonMap(EntityHandler.class, entityHandler);
        return ReflectionClass.stream(DataPointService.class.getPackage().getName(), DataPointService.class,
                                      ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs)).filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    default boolean enableTimeAudit() {
        return true;
    }

    default boolean enableFullResourceInCUDResponse() {
        return true;
    }

    default String api() {
        return "datapoint." + this.getClass().getSimpleName();
    }

    default String address() {
        return this.getClass().getName();
    }

    default Map<String, EventMethodDefinition> definitions() {
        return Collections.singletonMap(api(),
                                        EventMethodDefinition.createDefault(servicePath(), "/:" + requestKeyName()));
    }

    default String servicePath() {
        return "/" + Strings.toUrlPathWithLC(modelClass().getSimpleName());
    }

    Publisher publisher();

}
