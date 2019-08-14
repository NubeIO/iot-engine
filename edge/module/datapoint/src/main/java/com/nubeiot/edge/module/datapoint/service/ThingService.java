package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.ThingMetadata;
import com.nubeiot.iotdata.edge.model.tables.daos.ThingDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;
import com.nubeiot.iotdata.edge.model.tables.records.ThingRecord;

import lombok.NonNull;

public final class ThingService extends AbstractDataPointService<Integer, Thing, ThingRecord, ThingDao, ThingMetadata> {

    public ThingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public ThingMetadata metadata() {
        return ThingMetadata.INSTANCE;
    }

}
