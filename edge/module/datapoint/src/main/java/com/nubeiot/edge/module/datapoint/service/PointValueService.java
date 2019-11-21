package com.nubeiot.edge.module.datapoint.service;

import java.time.OffsetDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.RealtimeDataMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.dto.PointPriorityValue.PointValue;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

import lombok.NonNull;

public final class PointValueService extends AbstractOneToManyEntityService<PointValueData, PointValueMetadata>
    implements DataPointService<PointValueData, PointValueMetadata>, PointExtension {

    public PointValueService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public PointValueMetadata context() {
        return PointValueMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.CREATE, EventAction.PATCH, EventAction.REMOVE);
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
    private RequestData recomputeRequestData(RequestData reqData) {
        JsonObject filter = Optional.ofNullable(reqData.getFilter()).orElse(new JsonObject());
        if (!filter.getBoolean(Filters.AUDIT, false)) {
            filter.put(Filters.TEMP_AUDIT, true);
        }
        filter.put(Filters.AUDIT, true);
        return RequestData.builder()
                          .headers(reqData.headers())
                          .body(reqData.body())
                          .filter(filter)
                          .sort(reqData.getSort())
                          .pagination(reqData.getPagination())
                          .build();
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        final EventMethodDefinition d = EventMethodDefinition.create(
            Urls.combinePath(EntityHttpService.toCapturePath(PointMetadata.INSTANCE), servicePath()),
            ActionMethodMapping.byCRUD(getAvailableEvents()));
        return Stream.concat(DataPointService.super.definitions().stream(), Stream.of(d)).collect(Collectors.toSet());
    }

    @Override
    public JsonObject doTransform(EventAction action, Object key, VertxPojo pojo, RequestData reqData,
                                  BiFunction<VertxPojo, RequestData, JsonObject> converter) {
        if (Objects.nonNull(reqData.getFilter()) && reqData.getFilter().getBoolean(Filters.TEMP_AUDIT, false)) {
            reqData.getFilter().remove(Filters.AUDIT);
        }
        return super.doTransform(action, key, pojo, reqData, converter);
    }

    @Override
    protected Single<Entry<?, ? extends VertxPojo>> afterCreateOrUpdate(@NonNull RequestData reqData,
                                                                        @NonNull EventAction action,
                                                                        @NonNull Object primaryKey) {
        return doLookupByPrimaryKey(primaryKey).doOnSuccess(p -> syncPointValue(reqData, action, (PointValueData) p))
                                               .doOnEvent((p, e) -> invokeAsyncTask(reqData, action, p, e))
                                               .map(pojo -> new SimpleEntry<>(primaryKey, pojo));
    }

    private void syncPointValue(@NonNull RequestData reqData, @NonNull EventAction action,
                                @NonNull PointValueData pointValue) {
        final OffsetDateTime createdTime = action == EventAction.CREATE
                                           ? pointValue.getTimeAudit().getCreatedTime()
                                           : pointValue.getTimeAudit().getLastModifiedTime();
        final PointValueData reqValue = context().parseFromRequest(reqData.body());
        final PointValue requestValue = new PointValue(reqValue.getPriority(), reqValue.getValue());
        createHistoryData(pointValue, requestValue, createdTime);
        createRealtimeData(pointValue, requestValue, createdTime);
    }

    private void createRealtimeData(@NonNull PointValueData pv, @NonNull PointValue requestValue,
                                    @NonNull OffsetDateTime createdTime) {
        final JsonObject rtValue = RealtimeDataMetadata.simpleValue(requestValue.getValue(),
                                                                    requestValue.getPriority());
        send(RealtimeDataService.class,
             new PointRealtimeData().setPoint(pv.getPoint()).setValue(rtValue).setTime(createdTime).toJson());
    }

    private void createHistoryData(@NonNull PointValueData pv, @NonNull PointValue requestValue,
                                   @NonNull OffsetDateTime createdTime) {
        send(HistoryDataService.class, new PointHistoryData().setPoint(pv.getPoint())
                                                             .setValue(requestValue.getValue())
                                                             .setPriority(requestValue.getPriority())
                                                             .setTime(createdTime)
                                                             .toJson());
    }

    private void send(@NonNull Class<? extends DataPointService> serviceName, @NonNull JsonObject body) {
        entityHandler().eventClient()
                       .send(serviceName.getName(),
                             EventMessage.initial(EventAction.CREATE, RequestData.builder().body(body).build()));
    }

}
