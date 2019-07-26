package com.nubeiot.edge.connector.datapoint.service;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ModelService.BigSerialKeyModel;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointTagDao;
import com.nubeiot.iotdata.model.tables.pojos.PointTag;
import com.nubeiot.iotdata.model.tables.records.PointTagRecord;

import lombok.NonNull;

public final class TagPointService extends AbstractDittoService<Long, PointTag, PointTagRecord, PointTagDao>
    implements BigSerialKeyModel<PointTag, PointTagRecord, PointTagDao> {

    public TagPointService(PointTagDao dao) {
        super(dao);
    }

    @Override
    protected PointTag parse(@NonNull JsonObject request) throws IllegalArgumentException {
        return new PointTag(request);
    }

    @Override
    protected @NonNull String listKey() {
        return "tags";
    }

    @Override
    public @NonNull JsonTable<PointTagRecord> table() {
        return Tables.POINT_TAG;
    }

    @Override
    public String endpoint() {
        return null;
    }

}
