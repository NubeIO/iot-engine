package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.MeasureUnitMetadata;
import com.nubeiot.iotdata.edge.model.tables.daos.MeasureUnitDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.edge.model.tables.records.MeasureUnitRecord;

import lombok.NonNull;

public final class MeasureUnitService
    extends AbstractDataPointService<String, MeasureUnit, MeasureUnitRecord, MeasureUnitDao, MeasureUnitMetadata> {

    public MeasureUnitService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public MeasureUnitMetadata metadata() {
        return MeasureUnitMetadata.INSTANCE;
    }

}
