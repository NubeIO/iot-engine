package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.exceptions.DesiredException;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.MeasureUnitMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.RealtimeDataMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.unit.DataType;

import lombok.NonNull;

public final class RealtimeDataService extends AbstractOneToManyEntityService<PointRealtimeData, RealtimeDataMetadata>
    implements DataPointService<PointRealtimeData, RealtimeDataMetadata>, PointExtension {

    public RealtimeDataService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public RealtimeDataMetadata context() {
        return RealtimeDataMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE, EventAction.CREATE);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        final EventMethodDefinition definition = EventMethodDefinition.create("/point/:point_id/rt-data",
                                                                              "/:" + context().requestKeyName(),
                                                                              ActionMethodMapping.READ_MAP);
        return Stream.of(definition).collect(Collectors.toSet());
    }

    @Override
    protected Single<?> doInsert(@NonNull RequestData reqData) {
        final PointRealtimeData rtData = (PointRealtimeData) validation().onCreating(reqData);
        return validateReferenceEntity(reqData).flatMapSingle(b -> isAbleToCreate(rtData))
                                               .flatMap(b -> findDataType(rtData))
                                               .map(unit -> rtData.setValue(
                                                   RealtimeDataMetadata.fullValue(rtData.getValue(), unit)))
                                               .flatMap(rt -> queryExecutor().insertReturningPrimary(rt, reqData));
    }

    private Single<Boolean> isAbleToCreate(@NonNull PointRealtimeData rtData) {
        return entityHandler().dao(RealtimeSettingMetadata.INSTANCE.daoClass())
                              .findOneById(rtData.getPoint())
                              .filter(Optional::isPresent)
                              .map(Optional::get)
                              .map(s -> Optional.ofNullable(s.getEnabled()).orElse(false))
                              .defaultIfEmpty(false)
                              .filter(b -> b)
                              .switchIfEmpty(Single.error(new DesiredException(
                                  "Realtime setting of point " + rtData.getPoint() + " is disabled")));
    }

    private Single<DataType> findDataType(@NonNull PointRealtimeData rtData) {
        final EventController client = entityHandler().eventClient();
        final RequestData reqData = RequestData.builder()
                                               .body(new JsonObject().put(PointMetadata.INSTANCE.requestKeyName(),
                                                                          rtData.getPoint().toString()))
                                               .build();
        final DeliveryEvent event = DeliveryEvent.builder()
                                                 .address(PointService.class.getName())
                                                 .action(EventAction.GET_ONE)
                                                 .addPayload(reqData)
                                                 .build();
        return Single.<EventMessage>create(emitter -> {
            client.request(event, ReplyEventHandler.builder().action(EventAction.GET_ONE).address(event.getAddress())
                                                   .success(emitter::onSuccess)
                                                   .exception(emitter::onError)
                                                   .build());
        }).map(EventMessage::getData)
          .map(json -> json.getJsonObject(MeasureUnitMetadata.INSTANCE.singularKeyName(), new JsonObject()))
          .map(unit -> JsonData.convert(unit, DataType.class));
    }

}
