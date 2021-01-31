package com.nubeiot.core.rpc.discovery;

import java.util.Arrays;
import java.util.Collection;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.event.EventListener;
import io.github.zero88.qwe.protocol.Protocol;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.rpc.RpcProtocolClient;
import com.nubeiot.core.rpc.coordinator.InboundCoordinator;
import com.nubeiot.iotdata.IoTEntities;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents for a {@code RpcClient service} that is able to discover a particular {@code protocol} data object on
 * demand.
 * <p>
 * It also helps registering {@link InboundCoordinator} to one/many particular {@code protocol} data object if using
 *
 * @param <P> Type of IoT entity
 * @see RpcProtocolClient
 * @see Protocol
 * @see InboundCoordinator
 */
public interface RpcExplorer<K, P extends IoTEntity<K>, X extends IoTEntities<K, P>>
    extends RpcProtocolClient<P>, EventListener {

    @EventContractor(action = "GET_ONE", returnType = Single.class)
    Single<P> discover(RequestData reqData);

    @EventContractor(action = "GET_LIST", returnType = Single.class)
    Single<X> discoverMany(RequestData reqData);

    /**
     * Register a {@link InboundCoordinator} to one particular {@code protocol} data object
     *
     * @param data given data
     * @return json result in {@link Single}
     */
    @EventContractor(action = "CREATE", returnType = Single.class)
    Single<JsonObject> discoverThenWatch(@NonNull RequestData data);

    /**
     * Register many {@link InboundCoordinator} to many particular {@code protocol} data objects
     *
     * @param data given data
     * @return json result in {@link Single}
     */
    @EventContractor(action = "BATCH_CREATE", returnType = Single.class)
    Single<JsonObject> discoverManyThenWatch(@NonNull RequestData data);

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE, EventAction.CREATE, EventAction.BATCH_CREATE);
    }

}
