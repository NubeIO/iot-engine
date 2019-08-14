package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.OneToManyReferenceEntityService;
import com.nubeiot.edge.module.datapoint.service.Metadata.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.daos.RealtimeSettingDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;
import com.nubeiot.iotdata.edge.model.tables.records.RealtimeSettingRecord;

import lombok.NonNull;

public final class RealtimeSettingService extends
                                          AbstractDataPointService<UUID, RealtimeSetting, RealtimeSettingRecord,
                                                                      RealtimeSettingDao, RealtimeSettingMetadata>
    implements
    OneToManyReferenceEntityService<UUID, RealtimeSetting, RealtimeSettingRecord, RealtimeSettingDao,
                                       RealtimeSettingMetadata>,
    PointExtension {

    public RealtimeSettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public RealtimeSettingMetadata metadata() {
        return RealtimeSettingMetadata.INSTANCE;
    }

}
