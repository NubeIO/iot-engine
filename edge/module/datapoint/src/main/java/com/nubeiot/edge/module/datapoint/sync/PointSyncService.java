package com.nubeiot.edge.module.datapoint.sync;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.vertx.reactivex.RxHelper;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.EntityPostService;
import com.nubeiot.core.sql.service.EntityPostService.EntityPostServiceDelegate;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel;
import com.nubeiot.edge.module.datapoint.service.PointService;

import lombok.NonNull;

//TODO need to re-design... Because `transform` still depends on `EntityPostService type`...
public final class PointSyncService extends EntityPostServiceDelegate {

    @SuppressWarnings("unchecked")
    public PointSyncService(@NonNull EntityPostService delegate) {
        super(delegate);
    }

    @Override
    public @NonNull Maybe<IDittoModel<VertxPojo>> transform(@NonNull EntityService service, @NonNull VertxPojo data) {
        PointService ps = (PointService) service;
        final @NonNull EntityHandler handler = service.entityHandler();
        return Maybe.fromCallable(() -> IDittoModel.create(handler::sharedData, ps.contextGroup(), data.toJson()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSuccess(@NonNull EntityService service, @NonNull EventAction action, VertxPojo data,
                          @NonNull RequestData requestData) {
        transform(service, data).map(syncData -> doSyncOnSuccess(service, action, syncData, requestData))
                                .observeOn(RxHelper.blockingScheduler(service.entityHandler().vertx()))
                                .subscribe();
    }

}
