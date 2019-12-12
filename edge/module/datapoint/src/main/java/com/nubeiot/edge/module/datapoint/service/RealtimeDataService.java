package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.DesiredException;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.edge.module.datapoint.DataPointIndex.MeasureUnitMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.RealtimeDataMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.RealtimeSettingMetadata;
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
        return EntityHttpService.createDefinitions(ActionMethodMapping.DQL_MAP, this::servicePath,
                                                   context()::requestKeyName, PointMetadata.INSTANCE);
    }

    protected OperationValidator initCreationValidator() {
        return OperationValidator.create((req, prev) -> queryExecutor().mustExists(req)
                                                                       .map(b -> validation().onCreating(req))
                                                                       .map(PointRealtimeData.class::cast)
                                                                       .flatMap(this::isAbleToCreate)
                                                                       .flatMap(this::addDataType));
    }

    private Single<PointRealtimeData> isAbleToCreate(@NonNull PointRealtimeData rtData) {
        return entityHandler().dao(RealtimeSettingMetadata.INSTANCE.daoClass())
                              .findOneById(rtData.getPoint())
                              .filter(Optional::isPresent)
                              .map(Optional::get)
                              .map(s -> Optional.ofNullable(s.getEnabled()).orElse(false))
                              .defaultIfEmpty(false)
                              .filter(b -> b)
                              .map(b -> rtData)
                              .switchIfEmpty(Single.error(new DesiredException(
                                  "Realtime setting of point " + rtData.getPoint() + " is disabled")));
    }

    private Single<PointRealtimeData> addDataType(PointRealtimeData rt) {
        return findDataType(rt).map(unit -> rt.setValue(RealtimeDataMetadata.fullValue(rt.getValue(), unit)));
    }

    private Single<DataType> findDataType(@NonNull PointRealtimeData rtData) {
        final EventbusClient client = entityHandler().eventClient();
        final RequestData reqData = RequestData.builder()
                                               .body(new JsonObject().put(PointMetadata.INSTANCE.requestKeyName(),
                                                                          rtData.getPoint().toString()))
                                               .build();
        return client.request(PointService.class.getName(), EventMessage.initial(EventAction.GET_ONE, reqData.toJson()))
                     .map(EventMessage::getData)
                     .map(json -> json.getJsonObject(MeasureUnitMetadata.INSTANCE.singularKeyName(), new JsonObject()))
                     .map(unit -> JsonData.convert(unit, DataType.class));
    }

}
