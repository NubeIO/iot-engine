package com.nubeiot.edge.module.datapoint.service;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestFilter;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.OneToOneChildEntityHttpService;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.service.AbstractOneToOneChildEntityService;
import com.nubeiot.core.sql.service.CreateOrUpdateEntityService;
import com.nubeiot.core.sql.service.OneToOneChildEntityService;
import com.nubeiot.core.sql.workflow.step.CreationStep;
import com.nubeiot.core.sql.workflow.step.ModificationStep;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.RealtimeDataMetadata;
import com.nubeiot.edge.module.datapoint.service.extension.PointExtension.PointOneToOneExtension;
import com.nubeiot.iotdata.dto.PointPriorityValue.PointValue;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

import lombok.NonNull;

public final class PointValueService extends AbstractOneToOneChildEntityService<PointValueData, PointValueMetadata>
    implements DataPointService<PointValueData, PointValueMetadata>, PointOneToOneExtension,
               OneToOneChildEntityHttpService<PointValueData, PointValueMetadata>,
               CreateOrUpdateEntityService<PointValueData, PointValueMetadata> {

    public PointValueService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public PointValueMetadata context() {
        return PointValueMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return OneToOneChildEntityService.availableEvents(CreateOrUpdateEntityService.super.getAvailableEvents());
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return OneToOneChildEntityHttpService.super.definitions();
    }

    @EventContractor(action = EventAction.CREATE_OR_UPDATE, returnType = Single.class)
    public Single<JsonObject> createOrUpdate(RequestData requestData) {
        return CreateOrUpdateEntityService.super.createOrUpdate(requestData);
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(super.onCreatingOneResource(requestData));
    }

    @Override
    public @NonNull RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(super.onModifyingOneResource(requestData));
    }

    @NonNull
    public Single<JsonObject> afterGet(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return Single.just(JsonPojo.from(pojo).toJson(JsonData.MAPPER, ignoreFields(requestData)));
    }

    @Override
    public @NonNull Single<JsonObject> afterPatch(@NonNull Object key, @NonNull VertxPojo pojo,
                                                  @NonNull RequestData reqData) {
        return Single.just(doTransform(EventAction.PATCH, key, pojo, reqData,
                                       (p, r) -> JsonPojo.from(pojo).toJson(JsonData.MAPPER, ignoreFields(reqData))));
    }

    @Override
    public JsonObject doTransform(EventAction action, Object key, VertxPojo pojo, RequestData reqData,
                                  BiFunction<VertxPojo, RequestData, JsonObject> converter) {
        if (Objects.nonNull(reqData.filter()) && reqData.filter().hasTempAudit()) {
            reqData.filter().remove(RequestFilter.Filters.AUDIT);
        }
        return super.doTransform(action, key, pojo, reqData, converter);
    }

    @Override
    protected CreationStep initCreationStep() {
        return super.initCreationStep()
                    .onSuccess((action, kv) -> syncPointValue((PointValueData) kv.request(), action,
                                                              (PointValueData) kv.dbEntity()));
    }

    @Override
    protected ModificationStep initModificationStep(EventAction action) {
        return super.initModificationStep(action)
                    .onSuccess((reqData, act, output) -> syncPointValue(context().parseFromRequest(reqData.body()), act,
                                                                        (PointValueData) output.dbEntity()));
    }

    private void syncPointValue(@NonNull PointValueData prev, @NonNull EventAction action,
                                @NonNull PointValueData pointValue) {
        final OffsetDateTime createdTime = action == EventAction.CREATE
                                           ? pointValue.getTimeAudit().getCreatedTime()
                                           : pointValue.getTimeAudit().getLastModifiedTime();
        final PointValue requestValue = new PointValue(prev.getPriority(), prev.getValue());
        createHistoryData(pointValue, requestValue, createdTime);
        createRealtimeData(pointValue, requestValue, createdTime);
    }

    private void createRealtimeData(@NonNull PointValueData pv, @NonNull PointValue requestValue,
                                    @NonNull OffsetDateTime createdTime) {
        final Double value = Optional.ofNullable(requestValue.getValue()).orElse(pv.getValue());
        final int priority = Objects.isNull(requestValue.getValue()) ? pv.getPriority() : requestValue.getPriority();
        final JsonObject rtValue = RealtimeDataMetadata.simpleValue(value, priority);
        send(RealtimeDataService.class,
             new PointRealtimeData().setPoint(pv.getPoint()).setValue(rtValue).setTime(createdTime).toJson());
    }

    private void createHistoryData(@NonNull PointValueData pv, @NonNull PointValue requestValue,
                                   @NonNull OffsetDateTime createdTime) {
        final Double value = Optional.ofNullable(requestValue.getValue()).orElse(pv.getValue());
        final int priority = Objects.isNull(requestValue.getValue()) ? pv.getPriority() : requestValue.getPriority();
        send(HistoryDataService.class, new PointHistoryData().setPoint(pv.getPoint())
                                                             .setValue(value)
                                                             .setPriority(priority)
                                                             .setTime(createdTime)
                                                             .toJson());
    }

    private void send(@NonNull Class<? extends DataPointService> serviceName, @NonNull JsonObject body) {
        entityHandler().eventClient()
                       .send(serviceName.getName(),
                             EventMessage.initial(EventAction.CREATE, RequestData.builder().body(body).build()));
    }

    private @NonNull RequestData recomputeRequestData(RequestData reqData) {
        final RequestFilter filter = reqData.filter();
        if (!filter.hasAudit()) {
            filter.put(RequestFilter.Filters.TEMP_AUDIT, true);
        }
        filter.put(RequestFilter.Filters.AUDIT, true);
        return RequestData.builder()
                          .headers(reqData.headers())
                          .body(reqData.body())
                          .filter(filter)
                          .sort(reqData.sort())
                          .pagination(reqData.pagination())
                          .build();
    }

}
