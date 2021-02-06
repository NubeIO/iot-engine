package io.github.zero88.qwe.iot.connector.coordinator;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.iot.connector.BaseRpcProtocol;
import io.github.zero88.qwe.iot.data.IoTEntity;
import io.reactivex.Single;

import lombok.NonNull;

public abstract class AbstractOutboundCoordinator<P extends IoTEntity> extends BaseRpcProtocol<P>
    implements OutboundCoordinator<P> {

    protected AbstractOutboundCoordinator(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    @EventContractor(action = "CREATE", returnType = Single.class)
    public Single<P> create(@NonNull RequestData requestData) {
        final P pojo = parseEntity(requestData);
        return shouldSkip(pojo) ? Single.just(pojo) : doCreate(pojo);
    }

    @Override
    @EventContractor(action = "UPDATE", returnType = Single.class)
    public Single<P> update(@NonNull RequestData requestData) {
        final P pojo = parseEntity(requestData);
        return shouldSkip(pojo) ? Single.just(pojo) : doUpdate(pojo);
    }

    @Override
    @EventContractor(action = "PATCH", returnType = Single.class)
    public Single<P> patch(@NonNull RequestData requestData) {
        final P pojo = parseEntity(requestData);
        return shouldSkip(pojo) ? Single.just(pojo) : doPatch(pojo);
    }

    @Override
    @EventContractor(action = "REMOVE", returnType = Single.class)
    public Single<P> delete(@NonNull RequestData requestData) {
        final P pojo = parseEntity(requestData);
        return shouldSkip(pojo) ? Single.just(pojo) : doDelete(pojo);
    }

    protected abstract P parseEntity(@NonNull RequestData requestData);/* {
        return (P) context().parseFromRequest(requestData.body());
    }*/

    protected boolean shouldSkip(P pojo) {
        //        return !(context() instanceof HasProtocol) || !((HasProtocol) context()).getProtocol(pojo).equals
        //        (protocol());
        return true;
    }

    protected abstract Single<P> doCreate(@NonNull P pojo);

    protected abstract Single<P> doUpdate(@NonNull P pojo);

    protected abstract Single<P> doPatch(@NonNull P pojo);

    protected abstract Single<P> doDelete(@NonNull P pojo);

}
