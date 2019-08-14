package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.OneToManyReferenceEntityService;
import com.nubeiot.edge.module.datapoint.service.Metadata.RealtimeDataMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.daos.PointRealtimeDataDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.edge.model.tables.records.PointRealtimeDataRecord;

import lombok.NonNull;

public final class RealtimeDataService extends
                                       AbstractDataPointService<Long, PointRealtimeData, PointRealtimeDataRecord,
                                                                   PointRealtimeDataDao, RealtimeDataMetadata>
    implements
    OneToManyReferenceEntityService<Long, PointRealtimeData, PointRealtimeDataRecord, PointRealtimeDataDao,
                                       RealtimeDataMetadata>,
    PointExtension {

    public RealtimeDataService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public String servicePath() {
        return "realtime-data";
    }

    @Override
    public RealtimeDataMetadata metadata() {
        return RealtimeDataMetadata.INSTANCE;
    }

}
