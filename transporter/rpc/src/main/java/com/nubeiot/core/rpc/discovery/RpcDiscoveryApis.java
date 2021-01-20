package com.nubeiot.core.rpc.discovery;

import java.util.Collections;
import java.util.Set;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.micro.metadata.ActionMethodMapping;
import io.github.zero88.qwe.micro.metadata.EventHttpService;
import io.github.zero88.qwe.micro.metadata.EventMethodDefinition;
import io.github.zero88.utils.Urls;

import lombok.NonNull;

/**
 * Represents for {@code Discovery APIs} that expose as public endpoints
 *
 * @param <T> Type of Discovery APIs
 * @see RpcDiscovery
 * @see EventHttpService
 */
public interface RpcDiscoveryApis<P extends JsonData, T extends RpcDiscoveryApis>
    extends RpcDiscovery<P, T>, EventHttpService {

    /**
     * Base Discovery path
     */
    String BASE_PATH = "/com/nubeiot/core/rpc/discovery";

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
