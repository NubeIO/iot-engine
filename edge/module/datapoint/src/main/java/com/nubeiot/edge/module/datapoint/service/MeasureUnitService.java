package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.StringKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.MeasureUnitDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.edge.model.tables.records.MeasureUnitRecord;

import lombok.NonNull;

public final class MeasureUnitService
    extends AbstractDataPointService<String, MeasureUnit, MeasureUnitRecord, MeasureUnitDao>
    implements StringKeyEntity<MeasureUnit, MeasureUnitRecord, MeasureUnitDao> {

    public MeasureUnitService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
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
    public @NonNull String requestKeyName() {
        return jsonKeyName();
    }

}
