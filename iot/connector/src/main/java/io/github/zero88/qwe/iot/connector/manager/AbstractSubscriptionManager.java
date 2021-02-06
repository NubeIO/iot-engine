package io.github.zero88.qwe.iot.connector.manager;

import java.util.HashSet;
import java.util.Set;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.iot.connector.BaseRpcProtocol;
import io.github.zero88.qwe.iot.connector.coordinator.OutboundCoordinator;
import io.github.zero88.qwe.iot.data.IoTEntity;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public abstract class AbstractSubscriptionManager<P extends IoTEntity, S extends OutboundCoordinator<P>>
    extends BaseRpcProtocol<P> implements SubscriptionManager<P, S> {

    @Getter
    private final Set<S> subscribers = new HashSet<>();

    public AbstractSubscriptionManager(@NonNull SharedDataLocalProxy proxy) {
        super(proxy);
    }

}
