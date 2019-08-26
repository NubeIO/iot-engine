package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;

import com.nubeiot.core.event.EventAction;
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
    public String servicePath() {
        return "realtime-data";
    }

    @Override
    public RealtimeDataMetadata context() {
        return RealtimeDataMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.GET_LIST);
    }

}
