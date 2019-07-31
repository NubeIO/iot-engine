package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.workflow.ConsumerService;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.TransducersDao;
import com.nubeiot.iotdata.model.tables.pojos.Transducers;
import com.nubeiot.iotdata.model.tables.records.TransducersRecord;

import lombok.NonNull;

public final class TransducerService extends DataPointService<UUID, Transducers, TransducersRecord, TransducersDao>
    implements UUIDKeyEntity<Transducers, TransducersRecord, TransducersDao> {

    public TransducerService(@NonNull EntityHandler entityHandler, @NonNull HttpClientDelegate client) {
        super(entityHandler, client);
    }

    @Override
    protected @NonNull String listKey() {
        return "transducers";
    }

    @Override
    public @NonNull Class<Transducers> modelClass() {
        return Transducers.class;
    }

    @Override
    public @NonNull Class<TransducersDao> daoClass() {
        return TransducersDao.class;
    }

    @Override
    public @NonNull JsonTable<TransducersRecord> table() {
        return Tables.TRANSDUCERS;
    }

    @Override
    public String endpoint() {
        return null;
    }

}
