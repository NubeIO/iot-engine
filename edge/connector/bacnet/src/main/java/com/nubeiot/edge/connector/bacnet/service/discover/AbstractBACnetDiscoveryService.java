package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.dto.DiscoverOptions;
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
        methods.put(EventAction.DISCOVER, HttpMethod.POST);
        return Collections.singleton(
            EventMethodDefinition.create(servicePath(), paramPath(), ActionMethodMapping.create(methods)));
    }

    @NonNull
    protected abstract String servicePath();

    protected abstract String paramPath();

    protected final DiscoverOptions parseDiscoverOptions(@NonNull RequestData reqData) {
        final LocalDeviceMetadata metadata = getSharedDataValue(BACnetDevice.EDGE_BACNET_METADATA);
        return DiscoverOptions.from(metadata.getMaxTimeoutInMS(), reqData);
    }

}
