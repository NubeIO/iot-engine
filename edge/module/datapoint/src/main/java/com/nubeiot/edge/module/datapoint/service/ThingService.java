package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.SerialKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.ThingDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;
import com.nubeiot.iotdata.edge.model.tables.records.ThingRecord;

import lombok.NonNull;

public final class ThingService extends AbstractDataPointService<Integer, Thing, ThingRecord, ThingDao>
    implements SerialKeyEntity<Thing, ThingRecord, ThingDao> {

    public ThingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
        return "things";
    }

    @Override
    public @NonNull Class<Thing> modelClass() {
        return Thing.class;
    }

    @Override
    public @NonNull Class<ThingDao> daoClass() {
        return ThingDao.class;
    }

    @Override
    public @NonNull JsonTable<ThingRecord> table() {
        return Tables.THING;
    }

}
