package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;

import lombok.NonNull;

/**
 * Defines public service to expose HTTP API for end-user and/or nube-io service
 */
abstract class AbstractBACnetDiscoveryService extends AbstractSharedDataDelegate<AbstractBACnetDiscoveryService>
    implements BACnetDiscoveryService {

    AbstractBACnetDiscoveryService(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx);
        registerSharedKey(sharedKey);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        Map<EventAction, HttpMethod> methods = new HashMap<>();
        methods.put(EventAction.GET_LIST, HttpMethod.GET);
        methods.put(EventAction.GET_ONE, HttpMethod.GET);
        methods.put(EventAction.BATCH_CREATE, HttpMethod.POST);
        methods.put(EventAction.CREATE, HttpMethod.PUT);
        return Collections.singleton(
            EventMethodDefinition.create(servicePath(), paramPath(), ActionMethodMapping.create(methods)));
    }

    @NonNull
    protected abstract String servicePath();

    protected abstract String paramPath();

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public abstract Single<JsonObject> list(RequestData reqData);

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public abstract Single<JsonObject> get(RequestData reqData);

    @EventContractor(action = EventAction.BATCH_CREATE, returnType = Single.class)
    public abstract Single<JsonObject> batchPersist(RequestData reqData);

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public abstract Single<JsonObject> persist(RequestData reqData);

    protected final DiscoverOptions parseDiscoverOptions(@NonNull RequestData reqData) {
        final LocalDeviceMetadata metadata = getSharedDataValue(BACnetDevice.EDGE_BACNET_METADATA);
        return DiscoverOptions.from(metadata.getMaxTimeoutInMS(), reqData);
    }

    protected final CommunicationProtocol parseNetworkProtocol(@NonNull DiscoverRequest request) {
        final BACnetNetworkCache networkCache = getSharedDataValue(BACnetCacheInitializer.EDGE_NETWORK_CACHE);
        final CommunicationProtocol cacheProtocol = networkCache.get(request.getNetworkCode());
        final CommunicationProtocol reqBodyProtocol = BACnetNetwork.factory(request.getNetwork()).toProtocol();
        return JsonData.from(cacheProtocol.toJson().mergeIn(reqBodyProtocol.toJson()), CommunicationProtocol.class);
    }

}
