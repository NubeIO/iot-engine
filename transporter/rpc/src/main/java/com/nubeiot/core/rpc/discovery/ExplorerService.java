package com.nubeiot.core.rpc.discovery;

import java.util.Arrays;
import java.util.Collection;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.protocol.Protocol;
import io.reactivex.Single;

import com.nubeiot.core.rpc.ConnectorService;
import com.nubeiot.core.rpc.RpcProtocolClient;
import com.nubeiot.iotdata.IoTEntities;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents for an {@code explorer service} that is able to discover a particular {@code protocol} data object on
 * demand.
 *
 * @param <P> Type of IoT entity
 * @param <K> Type of IoT entity key
 * @param <X> Type of IoT entities that wraps IoT entity
 * @see RpcProtocolClient
 * @see Protocol
 */
public interface ExplorerService<K, P extends IoTEntity<K>, X extends IoTEntities<K, P>> extends ConnectorService {

    /**
     * Defines service function name that will be used to distinguish to other services.
     *
     * @return function name
     * @apiNote It is used to generated HTTP path and Event address then it must be unique
     */
    default String function() {
        return "discovery";
    }

    @EventContractor(action = "GET_ONE", returnType = Single.class)
    Single<P> discover(RequestData reqData);

    @EventContractor(action = "GET_LIST", returnType = Single.class)
    Single<X> discoverMany(RequestData reqData);

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE);
    }

}
