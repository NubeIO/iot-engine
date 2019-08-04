package com.nubeiot.edge.connector.datapoint.service;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.StringKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.MeasureUnitDao;
import com.nubeiot.iotdata.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.model.tables.records.MeasureUnitRecord;
import com.nubeiot.iotdata.unit.DataType;

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
    @NonNull
    public JsonObject customizeGetItem(@NonNull MeasureUnit pojo, @NonNull RequestData requestData) {
        return DataType.factory(pojo.getMeasureType(), pojo.getSymbol()).toJson();
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

}
