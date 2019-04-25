package com.nubeiot.edge.connector.datapoint;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.connector.datapoint.model.Tables;

import lombok.NonNull;

public final class DataPointEntityHandler extends EntityHandler {

    public DataPointEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        super(jooqConfig, vertx);
    }

    @Override
    public boolean isNew() {
        return isNew(Tables.POINT);
    }

    @Override
    public Single<EventMessage> initData() {
        return Single.just(EventMessage.initial(EventAction.INIT));
    }

    @Override
    public Single<EventMessage> migrate() {
        return Single.just(EventMessage.initial(EventAction.MIGRATE));
    }

}
