package com.nubeiot.edge.module.scheduler;

import org.jooq.Catalog;
import org.jooq.Configuration;

import io.reactivex.Observable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.SchemaHandler;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;
import com.nubeiot.edge.module.scheduler.pojos.JobTriggerComposite;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.JobConverter;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.TriggerConverter;
import com.nubeiot.iotdata.scheduler.model.DefaultCatalog;
import com.nubeiot.iotdata.scheduler.model.Keys;
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
    public @NonNull Catalog catalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public @NonNull SchemaHandler schemaHandler() {
        return new SchedulerSchemaHandler();
    }

    @Override
    public @NonNull Class keyClass() {
        return Keys.class;
    }

    //TODO need to handle error
    @SuppressWarnings("unchecked")
    Observable<EventMessage> register(@NonNull QuartzSchedulerContext schedulerContext) {
        final RequestData reqData = RequestData.builder().filter(new JsonObject().put("enable", true)).build();
        final EventbusClient client = eventClient();
        return complexQuery().from(JobTriggerMetadata.INSTANCE)
                             .context(JobEntityMetadata.INSTANCE)
                             .with(TriggerEntityMetadata.INSTANCE)
                             .viewPredicate(metadata -> true)
                             .findMany(reqData)
                             .map(jobTrigger -> createEvent((JobTriggerComposite) jobTrigger, schedulerContext))
                             .map(event -> client.request((DeliveryEvent) event));
    }

    private DeliveryEvent createEvent(@NonNull JobTriggerComposite composite, @NonNull QuartzSchedulerContext context) {
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

}
