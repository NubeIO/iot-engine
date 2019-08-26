package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;

import lombok.NonNull;

public final class RealtimeSettingService
    extends AbstractOneToManyEntityService<RealtimeSetting, RealtimeSettingMetadata>
    implements PointExtension, DataPointService<RealtimeSetting, RealtimeSettingMetadata> {

    public RealtimeSettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public RealtimeSettingMetadata context() {
        return RealtimeSettingMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.CREATE, EventAction.UPDATE, EventAction.PATCH,
                             EventAction.REMOVE);
    }

}
