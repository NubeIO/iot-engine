package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

public interface DataPointService<M extends EntityMetadata, V extends EntityValidation>
    extends EntityService<M, V>, EventHttpService, EntityValidation, EntityTransformer {

    static Set<? extends DataPointService> createServices(AbstractEntityHandler entityHandler) {
        final Map<Class, Object> inputs = Collections.singletonMap(AbstractEntityHandler.class, entityHandler);
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
        return Collections.singleton(
            EventMethodDefinition.createDefault(servicePath(), "/:" + metadata().requestKeyName()));
    }

    default String servicePath() {
        return "/" + Strings.toUrlPathWithLC(metadata().modelClass().getSimpleName());
    }

    @Override
    @SuppressWarnings("unchecked")
    default V validation() {
        return (V) this;
    }

}
