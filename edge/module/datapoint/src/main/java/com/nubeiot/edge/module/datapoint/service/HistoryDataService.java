package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.OneToManyReferenceEntityService;
import com.nubeiot.edge.module.datapoint.service.Metadata.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.daos.PointHistoryDataDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.records.PointHistoryDataRecord;

import lombok.NonNull;

public final class HistoryDataService extends
                                      AbstractDataPointService<Long, PointHistoryData, PointHistoryDataRecord,
                                                                  PointHistoryDataDao, HistoryDataMetadata>
    implements PointExtension,
               OneToManyReferenceEntityService<Long, PointHistoryData, PointHistoryDataRecord, PointHistoryDataDao,
                                                  HistoryDataMetadata> {

    public HistoryDataService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public String servicePath() {
        return "/histories";
    }

    @Override
    public HistoryDataMetadata metadata() {
        return HistoryDataMetadata.INSTANCE;
    }

}
