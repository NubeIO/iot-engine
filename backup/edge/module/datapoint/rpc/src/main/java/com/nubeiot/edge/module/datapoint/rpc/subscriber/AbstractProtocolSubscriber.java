package com.nubeiot.edge.module.datapoint.rpc.subscriber;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.edge.module.datapoint.model.pojos.HasProtocol;
import com.nubeiot.edge.module.datapoint.rpc.BaseRpcProtocol;

import lombok.NonNull;

public abstract class AbstractProtocolSubscriber<P extends VertxPojo>
    extends BaseRpcProtocol<AbstractProtocolSubscriber> implements DataProtocolSubscriber<P> {

    protected AbstractProtocolSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<P> create(@NonNull RequestData requestData) {
        final P pojo = parseEntity(requestData);
        return shouldSkip(pojo) ? Single.just(pojo) : doCreate(pojo);
    }

    @Override
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<P> update(@NonNull RequestData requestData) {
        final P pojo = parseEntity(requestData);
        return shouldSkip(pojo) ? Single.just(pojo) : doUpdate(pojo);
    }

    @Override
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<P> patch(@NonNull RequestData requestData) {
        final P pojo = parseEntity(requestData);
        return shouldSkip(pojo) ? Single.just(pojo) : doPatch(pojo);
    }

    @Override
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<P> delete(@NonNull RequestData requestData) {
        final P pojo = parseEntity(requestData);
        return shouldSkip(pojo) ? Single.just(pojo) : doDelete(pojo);
    }

    @SuppressWarnings("unchecked")
    protected P parseEntity(@NonNull RequestData requestData) {
        return (P) context().parseFromRequest(requestData.body());
    }

    @SuppressWarnings("unchecked")
    protected boolean shouldSkip(P pojo) {
        return !(context() instanceof HasProtocol) || !((HasProtocol) context()).getProtocol(pojo).equals(protocol());
    }

    protected abstract Single<P> doCreate(@NonNull P pojo);

    protected abstract Single<P> doUpdate(@NonNull P pojo);

    protected abstract Single<P> doPatch(@NonNull P pojo);

    protected abstract Single<P> doDelete(@NonNull P pojo);

}
