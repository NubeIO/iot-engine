package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.MeasureUnitDao;
import com.nubeiot.iotdata.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.model.tables.records.MeasureUnitRecord;

import lombok.NonNull;

public final class MeasureUnitService extends AbstractDittoService<UUID, MeasureUnit, MeasureUnitRecord, MeasureUnitDao>
    implements UUIDKeyEntity<MeasureUnit, MeasureUnitRecord, MeasureUnitDao> {

    public MeasureUnitService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    protected @NonNull String listKey() {
        return "units";
    }

    @Override
    public @NonNull Class<MeasureUnit> modelClass() {
        return MeasureUnit.class;
    }

    @Override
    public @NonNull Class<MeasureUnitDao> daoClass() {
        return MeasureUnitDao.class;
    }

    @Override
    public @NonNull JsonTable<MeasureUnitRecord> table() {
        return Tables.MEASURE_UNIT;
    }

    @Override
    public String endpoint() { return null; }

}
