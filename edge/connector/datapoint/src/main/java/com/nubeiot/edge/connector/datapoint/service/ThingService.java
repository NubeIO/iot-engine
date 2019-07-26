package com.nubeiot.edge.connector.datapoint.service;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ModelService.SerialKeyModel;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.ThingDao;
import com.nubeiot.iotdata.model.tables.pojos.Thing;
import com.nubeiot.iotdata.model.tables.records.ThingRecord;

import lombok.NonNull;

public final class ThingService extends AbstractDittoService<Integer, Thing, ThingRecord, ThingDao>
    implements SerialKeyModel<Thing, ThingRecord, ThingDao> {

    public ThingService(ThingDao dao) {
        super(dao);
    }

    @Override
    protected Thing parse(@NonNull JsonObject request) throws IllegalArgumentException {
        return new Thing(request);
    }

    @Override
    protected @NonNull String listKey() {
        return "things";
    }

    @Override
    public @NonNull JsonTable<ThingRecord> table() {
        return Tables.THING;
    }

    @Override
    public String endpoint() {
        return null;
    }

}
