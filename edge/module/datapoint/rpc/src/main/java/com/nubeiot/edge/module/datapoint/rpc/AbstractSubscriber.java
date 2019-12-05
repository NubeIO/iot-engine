package com.nubeiot.edge.module.datapoint.rpc;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;

import lombok.NonNull;

public abstract class AbstractSubscriber<P extends VertxPojo> extends AbstractSharedDataDelegate<AbstractSubscriber>
    implements DataPointSubscriber<P> {

    protected AbstractSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx);
        this.registerSharedKey(sharedKey);
    }

    @Override
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public abstract Single<P> create(@NonNull RequestData requestData);

    @Override
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public abstract Single<P> update(@NonNull RequestData requestData);

    @Override
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public abstract Single<P> patch(@NonNull RequestData requestData);

    @Override
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public abstract Single<P> delete(@NonNull RequestData requestData);

}
