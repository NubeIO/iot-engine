package com.nubeiot.edge.module.datapoint.rpc.subscriber;

import java.util.HashSet;
import java.util.Set;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.Vertx;

import com.nubeiot.edge.module.datapoint.rpc.BaseRpcProtocol;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public abstract class AbstractProtocolSubscription<T extends AbstractProtocolSubscription,
                                                      S extends DataProtocolSubscriber<VertxPojo>>
    extends BaseRpcProtocol<T> implements DataProtocolSubscription<T, S> {

    @Getter
    private final Set<S> subscribers = new HashSet<>();

    public AbstractProtocolSubscription(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

}
