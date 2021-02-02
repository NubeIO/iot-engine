package com.nubeiot.core.rpc;

import java.util.Collections;
import java.util.Set;

import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.github.zero88.qwe.micro.http.EventHttpService;
import io.github.zero88.qwe.micro.http.EventMethodDefinition;
import io.github.zero88.utils.Urls;

import lombok.NonNull;

public interface ConnectorServiceApis extends ConnectorService, EventHttpService {

    /**
     * Base HTTP service path
     *
     * @return base HTTP service path
     */
    default String basePath() {
        return Urls.combinePath(function(), protocol().type().toLowerCase(), servicePath());
    }

    @Override
    default String api() {
        return String.join(".", function(), protocol().type().toLowerCase(), getClass().getSimpleName());
    }

    @Override
    default Set<EventMethodDefinition> definitions() {
        return Collections.singleton(EventMethodDefinition.create(basePath(), paramPath(), eventMethodMap()));
    }

    /**
     * Service discovery HTTP path for a specific protocol resource
     *
     * @return path
     */
    @NonNull String servicePath();

    /**
     * Parameter path for manipulating a specific protocol resource
     *
     * @return param path
     */
    String paramPath();

    /**
     * Event action and HTTP method mapping
     *
     * @return event method map
     * @see ActionMethodMapping
     * @see ConnectorService#getAvailableEvents()
     */
    @NonNull ActionMethodMapping eventMethodMap();

}
