package com.nubeiot.edge.module.scheduler;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata;
import com.nubeiot.iotdata.scheduler.model.Keys;
import com.nubeiot.iotdata.scheduler.model.Tables;

import lombok.NonNull;

class SchedulerEntityHandler extends AbstractEntityHandler
    implements AuditDecorator, EntityConstraintHolder, SchedulerMetadata {

    public SchedulerEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        super(jooqConfig, vertx);
    }

    @Override
    public boolean isNew() {
        return isNew(Tables.JOB_TRIGGER);
    }

    @Override
    public Single<EventMessage> initData() {
        return Single.just(EventMessage.success(EventAction.INIT));
    }

    @Override
    public Single<EventMessage> migrate() {
        return Single.just(EventMessage.success(EventAction.MIGRATE));
    }

    @Override
    public @NonNull Class keyClass() {
        return Keys.class;
    }

}
