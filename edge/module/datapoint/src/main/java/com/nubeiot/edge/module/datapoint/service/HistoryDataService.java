package com.nubeiot.edge.module.datapoint.service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;

import lombok.NonNull;

public final class HistoryDataService extends AbstractOneToManyEntityService<PointHistoryData, HistoryDataMetadata>
    implements PointExtension, DataPointService<PointHistoryData, HistoryDataMetadata> {

    public HistoryDataService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public HistoryDataMetadata context() {
        return HistoryDataMetadata.INSTANCE;
    }

    @Override
    public String servicePath() {
        return "/histories";
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return ActionMethodMapping.READ_MAP.get().keySet();
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        final EventMethodDefinition definition = EventMethodDefinition.create("/point/:point_id/histories",
                                                                              "/:" + context().requestKeyName(),
                                                                              ActionMethodMapping.READ_MAP);
        return Stream.concat(Stream.of(definition), DataPointService.super.definitions().stream())
                     .collect(Collectors.toSet());
    }

}
