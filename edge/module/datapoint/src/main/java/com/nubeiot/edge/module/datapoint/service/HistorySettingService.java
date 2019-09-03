package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;

import lombok.NonNull;

public final class HistorySettingService extends AbstractOneToManyEntityService<HistorySetting, HistorySettingMetadata>
    implements PointExtension, DataPointService<HistorySetting, HistorySettingMetadata> {

    public HistorySettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public HistorySettingMetadata context() {
        return HistorySettingMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.CREATE, EventAction.PATCH, EventAction.REMOVE);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        Map<EventAction, HttpMethod> crud = ActionMethodMapping.CRUD_MAP.get();
        ActionMethodMapping map = ActionMethodMapping.create(
            getAvailableEvents().stream().filter(crud::containsKey).collect(Collectors.toMap(e -> e, crud::get)));
        return Stream.concat(DataPointService.super.definitions().stream(),
                             Stream.of(EventMethodDefinition.create("/point/:point_id/history-setting", map)))
                     .collect(Collectors.toSet());
    }

}
