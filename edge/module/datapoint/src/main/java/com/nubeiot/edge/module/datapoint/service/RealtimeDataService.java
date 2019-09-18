package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.RealtimeDataMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;

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

}
