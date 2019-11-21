package com.nubeiot.edge.module.scheduler.service;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.query.ComplexQueryExecutor;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.scheduler.pojos.JobTriggerComposite;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobTriggerMetadata;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.JobConverter;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.TriggerConverter;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.scheduler.QuartzSchedulerContext;
import com.nubeiot.scheduler.SchedulerRequestData;
import com.nubeiot.scheduler.job.JobModel;
import com.nubeiot.scheduler.trigger.TriggerModel;

import lombok.Getter;
import lombok.NonNull;

abstract class JobTriggerCompositeService
    extends AbstractManyToManyEntityService<JobTriggerComposite, JobTriggerMetadata>
    implements SchedulerService<JobTriggerComposite, JobTriggerMetadata> {

    @Getter
    private QuartzSchedulerContext schedulerContext;

    JobTriggerCompositeService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler);
        this.schedulerContext = schedulerContext;
    }

    @Override
    public JobTriggerMetadata context() {
        return JobTriggerMetadata.INSTANCE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ComplexQueryExecutor<JobTriggerComposite> queryExecutor() {
        return super.queryExecutor().viewPredicate(metadata -> true);
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createCRUDDefinitions(resource(), reference());
    }

    @Override
    public @NonNull Single<JsonObject> afterEachList(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return Single.just(JsonPojo.from(pojo)
                                   .toJson(Stream.concat(ignoreFields(requestData).stream(),
                                                         Stream.of(reference().singularKeyName()))
                                                 .collect(Collectors.toSet())));
    }

    @Override
    public @NonNull Single<JsonObject> afterGet(@NonNull VertxPojo pojo, @NonNull RequestData reqData) {
        JobTriggerComposite composite = (JobTriggerComposite) pojo;
        if (!composite.getEnabled()) {
            return super.afterGet(pojo, reqData);
        }
        final DeliveryEvent event = createDeliveryEvent(composite, EventAction.GET_ONE);
        final EventbusClient client = entityHandler().eventClient();
        return Single.create(emitter -> client.fire(event, replyHandler(event, msg -> emitter.onSuccess(
            onSuccess(composite, reqData, msg)), error -> emitter.onError(error.getThrowable()))));
    }

    @Override
    public @NonNull Single<JsonObject> afterCreate(@NonNull Object key, @NonNull VertxPojo pojo,
                                                   @NonNull RequestData reqData) {
        JobTriggerComposite composite = (JobTriggerComposite) pojo;
        if (!composite.getEnabled()) {
            return super.afterCreate(key, pojo, reqData);
        }
        if (!enableFullResourceInCUDResponse()) {
            return Single.just(EntityTransformer.keyResponse(resourceMetadata().requestKeyName(), key));
        }
        final DeliveryEvent event = createDeliveryEvent(composite, EventAction.CREATE);
        final EventbusClient client = entityHandler().eventClient();
        return Single.create(emitter -> client.fire(event, replyHandler(event, msg -> emitter.onSuccess(
            cudResponse(composite, reqData, msg)), error -> emitter.onError(error.getThrowable()))));
    }

    @Override
    public @NonNull Single<JsonObject> afterPatch(@NonNull Object key, @NonNull VertxPojo pojo,
                                                  @NonNull RequestData reqData) {
        return super.afterPatch(key, pojo, reqData);
    }

    @Override
    public @NonNull Single<JsonObject> afterDelete(@NonNull VertxPojo pojo, @NonNull RequestData reqData) {
        return super.afterDelete(pojo, reqData);
    }

    protected abstract TriggerEntity getTrigger(JobTriggerComposite composite);

    protected abstract JobEntity getJob(JobTriggerComposite composite);

    private DeliveryEvent createDeliveryEvent(@NonNull JobTriggerComposite composite, @NonNull EventAction getOne) {
        final JobModel job = JobConverter.convert(getJob(composite));
        final TriggerModel trigger = TriggerConverter.convert(getTrigger(composite));
        return DeliveryEvent.builder()
                            .address(schedulerContext.getRegisterModel().getAddress())
                            .pattern(EventPattern.REQUEST_RESPONSE)
                            .action(getOne)
                            .payload(SchedulerRequestData.create(job, trigger).toJson())
                            .build();
    }

    private ReplyEventHandler replyHandler(DeliveryEvent event, Consumer<EventMessage> response,
                                           Consumer<ErrorMessage> onError) {
        return ReplyEventHandler.builder()
                                .system("EDGE_SCHEDULER")
                                .address(event.getAddress())
                                .action(event.getAction())
                                .success(response)
                                .error(onError)
                                .build();
    }

    private JsonObject onSuccess(@NonNull JobTriggerComposite composite, @NonNull RequestData reqData,
                                 @NonNull EventMessage msg) {
        return JsonPojo.from(composite).toJson(JsonPojo.MAPPER, ignoreFields(reqData)).mergeIn(msg.getData(), true);
    }

    private JsonObject cudResponse(@NonNull JobTriggerComposite composite, @NonNull RequestData reqData,
                                   @NonNull EventMessage msg) {
        return EntityTransformer.fullResponse(msg.getAction(), onSuccess(composite, reqData, msg));
    }

}
