package com.nubeiot.edge.module.monitor.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.utils.Reflections.ReflectionClass;
import io.github.zero88.utils.Urls;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;

import lombok.NonNull;

public interface MonitorService extends EventHttpService {

    static Set<? extends MonitorService> createServices() {
        return ReflectionClass.stream(MonitorService.class.getPackage().getName(), MonitorService.class,
                                      ReflectionClass.publicClass())
                              .map(ReflectionClass::createObject)
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    default String api() {
        return "bios.monitor." + this.getClass().getSimpleName();
    }

    default Set<EventMethodDefinition> definitions() {
        ActionMethodMapping map = () -> Collections.singletonMap(EventAction.GET_LIST, HttpMethod.GET);
        return Collections.singleton(EventMethodDefinition.create(Urls.combinePath(rootPath(), servicePath()), map));
    }

    default String rootPath() {
        return "/monitor";
    }

    String servicePath();

    @Override
    default @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.GET_LIST);
    }

}
