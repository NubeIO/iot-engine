package com.nubeiot.edge.core.service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;

public interface InstallerService extends EventHttpService {

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
