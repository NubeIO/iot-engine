package com.nubeiot.edge.connector.bacnet.service;

import java.util.Collections;
import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;

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
        final ActionMethodMapping mapping = ActionMethodMapping.create(
            Collections.singletonMap(EventAction.DISCOVER, HttpMethod.POST));
        return Collections.singleton(EventMethodDefinition.create(servicePath(), paramPath(), mapping));
    }

    @NonNull
    protected abstract String servicePath();

    protected abstract String paramPath();

}
