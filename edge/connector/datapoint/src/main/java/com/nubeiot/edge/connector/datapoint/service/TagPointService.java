package com.nubeiot.edge.connector.datapoint.service;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.BigSerialKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.workflow.ConsumerService;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.PointTagDao;
import com.nubeiot.iotdata.model.tables.pojos.PointTag;
import com.nubeiot.iotdata.model.tables.records.PointTagRecord;

import lombok.NonNull;

public final class TagPointService extends DataPointService<Long, PointTag, PointTagRecord, PointTagDao>
    implements BigSerialKeyEntity<PointTag, PointTagRecord, PointTagDao> {

    public TagPointService(@NonNull EntityHandler entityHandler, @NonNull HttpClientDelegate client) {
        super(entityHandler, client);
    }

    @Override
    protected @NonNull String listKey() {
        return "tags";
    }

    @Override
    public @NonNull Class<PointTag> modelClass() {
        return PointTag.class;
    }

    @Override
    public @NonNull Class<PointTagDao> daoClass() {
        return PointTagDao.class;
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
