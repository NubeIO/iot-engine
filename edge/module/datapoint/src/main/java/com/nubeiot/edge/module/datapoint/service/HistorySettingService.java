package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.OneToManyReferenceEntityService;
import com.nubeiot.edge.module.datapoint.service.Metadata.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.daos.HistorySettingDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.edge.model.tables.records.HistorySettingRecord;

import lombok.NonNull;

public final class HistorySettingService extends
                                         AbstractDataPointService<UUID, HistorySetting, HistorySettingRecord,
                                                                     HistorySettingDao, HistorySettingMetadata>
    implements PointExtension,
               OneToManyReferenceEntityService<UUID, HistorySetting, HistorySettingRecord, HistorySettingDao,
                                                  HistorySettingMetadata> {

    public HistorySettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public HistorySettingMetadata metadata() {
        return HistorySettingMetadata.INSTANCE;
    }

}
