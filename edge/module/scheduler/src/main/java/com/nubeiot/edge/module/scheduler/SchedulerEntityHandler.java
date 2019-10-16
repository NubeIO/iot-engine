package com.nubeiot.edge.module.scheduler;

import java.util.Optional;

import org.jooq.Configuration;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;
import com.nubeiot.edge.module.scheduler.pojos.JobTriggerComposite;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.JobConverter;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.TriggerConverter;
import com.nubeiot.iotdata.scheduler.model.Keys;
import com.nubeiot.iotdata.scheduler.model.Tables;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.scheduler.QuartzSchedulerContext;
import com.nubeiot.scheduler.SchedulerRequestData;
import com.nubeiot.scheduler.job.JobModel;
import com.nubeiot.scheduler.trigger.TriggerModel;

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

    //TODO need to handle error
    @SuppressWarnings("unchecked")
    Observable register(@NonNull QuartzSchedulerContext schedulerContext) {
        final RequestData reqData = RequestData.builder().filter(new JsonObject().put("enable", true)).build();
        final EventController client = eventClient();
        return complexQuery().from(JobTriggerMetadata.INSTANCE)
                             .context(JobEntityMetadata.INSTANCE)
                             .with(TriggerEntityMetadata.INSTANCE)
                             .viewPredicate(metadata -> true)
                             .findMany(reqData)
                             .map(jobTrigger -> event((JobTriggerComposite) jobTrigger, schedulerContext))
                             .doOnEach(no -> Optional.ofNullable(((Notification<DeliveryEvent>) no).getValue())
                                                     .ifPresent(event -> client.request(event, replyHandler(event))));
    }

    private DeliveryEvent event(@NonNull JobTriggerComposite composite, @NonNull QuartzSchedulerContext context) {
        final JobModel job = JobConverter.convert(
            composite.safeGetOther(JobEntityMetadata.INSTANCE.singularKeyName(), JobEntity.class));
        final TriggerModel trigger = TriggerConverter.convert(
            composite.safeGetOther(TriggerEntityMetadata.INSTANCE.singularKeyName(), TriggerEntity.class));
        return DeliveryEvent.builder()
                            .address(context.getRegisterModel().getAddress())
                            .pattern(EventPattern.REQUEST_RESPONSE)
                            .action(EventAction.CREATE)
                            .payload(SchedulerRequestData.create(job, trigger).toJson())
                            .build();
    }

    private ReplyEventHandler replyHandler(DeliveryEvent event) {
        return ReplyEventHandler.builder()
                                .system("EDGE_SCHEDULER").address(event.getAddress()).action(event.getAction())
                                .success(msg -> logger.info(msg.toJson()))
                                .error(error -> logger.error(error.toJson()))
                                .build();
    }

}
