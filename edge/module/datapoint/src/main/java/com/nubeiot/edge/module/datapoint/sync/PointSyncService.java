package com.nubeiot.edge.module.datapoint.sync;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
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
    public EntitySyncData transform(@NonNull EntityService service, @NonNull VertxPojo data) {
        PointService pService = (PointService) service;
        return (IDittoModel<?>) IDittoModel.create(pService.contextGroup(), data.toJson());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSuccess(@NonNull EntityService service, @NonNull EventAction action, VertxPojo data,
                          @NonNull RequestData requestData) {
        getDelegate().doSyncOnSuccess(service, action, transform(service, data), requestData);
    }

}
