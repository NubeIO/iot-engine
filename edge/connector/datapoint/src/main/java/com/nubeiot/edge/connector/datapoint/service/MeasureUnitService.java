package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ModelService.UUIDKeyModel;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.MeasureUnitDao;
import com.nubeiot.iotdata.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.model.tables.records.MeasureUnitRecord;

import lombok.NonNull;

public final class MeasureUnitService extends AbstractDittoService<UUID, MeasureUnit, MeasureUnitRecord, MeasureUnitDao>
    implements UUIDKeyModel<MeasureUnit, MeasureUnitRecord, MeasureUnitDao> {

    public MeasureUnitService(MeasureUnitDao dao) {
        super(dao);
    }

    @Override
    protected @NonNull String listKey() {
        return "units";
    }

    @Override
    public @NonNull Class<MeasureUnit> model() {
        return MeasureUnit.class;
    }

    @Override
    public @NonNull JsonTable<MeasureUnitRecord> table() {
        return Tables.MEASURE_UNIT;
    }

    @Override
    public String endpoint() { return null; }

}
