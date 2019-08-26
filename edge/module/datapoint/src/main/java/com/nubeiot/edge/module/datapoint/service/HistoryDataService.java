package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;

import com.nubeiot.core.event.EventAction;
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
        return Arrays.asList(EventAction.GET_ONE, EventAction.GET_LIST);
    }

}
