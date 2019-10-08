package com.nubeiot.edge.installer.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.edge.installer.InstallerEntityHandler;

public interface InstallerService extends EventHttpService {

    static Set<? extends InstallerService> createServices(InstallerEntityHandler entityHandler,
                                                          Class<? extends InstallerService> serviceClazz) {
        final Map<Class, Object> inputs = Collections.singletonMap(InstallerEntityHandler.class, entityHandler);
        return ReflectionClass.stream(serviceClazz.getPackage().getName(), serviceClazz, ReflectionClass.publicClass())
                              .map(clazz -> ReflectionClass.createObject(clazz, inputs))
                              .filter(Objects::nonNull)
                              .collect(Collectors.toSet());
    }

    @Override
    default Set<EventMethodDefinition> definitions() {
        Map<EventAction, HttpMethod> map = ActionMethodMapping.CRUD_MAP.get();
        ActionMethodMapping actionMethodMap = ActionMethodMapping.create(
            getAvailableEvents().stream().filter(map::containsKey).collect(Collectors.toMap(e -> e, map::get)));
        return Collections.singleton(
            EventMethodDefinition.create(Urls.combinePath(rootPath(), servicePath()), paramPath(), actionMethodMap));
    }

    String rootPath();

    String servicePath();

    String paramPath();

}
