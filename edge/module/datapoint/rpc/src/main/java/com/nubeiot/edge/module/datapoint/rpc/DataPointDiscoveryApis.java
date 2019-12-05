package com.nubeiot.edge.module.datapoint.rpc;

import java.util.Collections;
import java.util.Set;

import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;

import lombok.NonNull;

/**
 * Represents for {@code Discovery APIs} that expose as public endpoints
 *
 * @param <T> Type of Discovery APIs
 * @see DataPointDiscovery
 * @see EventHttpService
 */
public interface DataPointDiscoveryApis<T extends DataPointDiscoveryApis>
    extends DataPointDiscovery<T>, EventHttpService {

    /**
     * Base Discovery path
     */
    String BASE_PATH = "/discovery";

    @Override
    default Set<EventMethodDefinition> definitions() {
        final String path = Urls.combinePath(BASE_PATH, protocol().type().toLowerCase(), servicePath());
        return Collections.singleton(EventMethodDefinition.create(path, paramPath(), eventMethodMap()));
    }

    /**
     * Service Discovery path
     *
     * @return path
     */
    @NonNull String servicePath();

    /**
     * Parameter path for manipulating resource
     *
     * @return param path
     */
    String paramPath();

    /**
     * Event action and HTTP method mapping
     *
     * @return event method map
     * @see ActionMethodMapping
     */
    @NonNull ActionMethodMapping eventMethodMap();

}
