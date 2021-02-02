package com.nubeiot.core.rpc.discovery;

import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.github.zero88.qwe.micro.http.EventHttpService;

import com.nubeiot.core.rpc.ConnectorServiceApis;
import com.nubeiot.iotdata.IoTEntities;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents for {@code Discovery APIs} that expose as public endpoints
 *
 * @param <P> Type of IoT entity
 * @see ExplorerService
 * @see EventHttpService
 */
public interface ExplorerServiceApis<K, P extends IoTEntity<K>, X extends IoTEntities<K, P>>
    extends ExplorerService<K, P, X>, ConnectorServiceApis {

    /**
     * Event action and HTTP method mapping
     *
     * @return event method map
     * @see ActionMethodMapping
     */
    default @NonNull ActionMethodMapping eventMethodMap() {
        return ActionMethodMapping.DQL_MAP;
    }

}
