package com.nubeiot.core.rpc.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.github.zero88.qwe.micro.http.EventHttpService;
import io.github.zero88.qwe.micro.http.EventMethodDefinition;
import io.github.zero88.utils.Urls;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.iotdata.IoTEntities;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents for {@code Discovery APIs} that expose as public endpoints
 *
 * @param <P> Type of IoT entity
 * @see RpcExplorer
 * @see EventHttpService
 */
public interface RpcExplorerApis<K, P extends IoTEntity<K>, X extends IoTEntities<K, P>>
    extends RpcExplorer<K, P, X>, EventHttpService {

    @Override
    default Set<EventMethodDefinition> definitions() {
        final String path = Urls.combinePath(basePath(), protocol().type().toLowerCase(), servicePath());
        return Collections.singleton(EventMethodDefinition.create(path, paramPath(), eventMethodMap()));
    }

    default String basePath() {
        return "/rpc/discovery";
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
    default @NonNull ActionMethodMapping eventMethodMap() {
        Map<EventAction, HttpMethod> methods = new HashMap<>();
        methods.put(EventAction.GET_LIST, HttpMethod.GET);
        methods.put(EventAction.GET_ONE, HttpMethod.GET);
        methods.put(EventAction.CREATE, HttpMethod.POST);
        methods.put(EventAction.BATCH_CREATE, HttpMethod.PUT);
        return ActionMethodMapping.create(methods);
    }

}
