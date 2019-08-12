package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.TransducerDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;
import com.nubeiot.iotdata.edge.model.tables.records.TransducerRecord;

import lombok.NonNull;

public final class TransducerService extends AbstractDataPointService<UUID, Transducer, TransducerRecord, TransducerDao>
    implements UUIDKeyEntity<Transducer, TransducerRecord, TransducerDao> {

    public TransducerService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
        return "transducers";
    }

    @Override
    public @NonNull Class<Transducer> modelClass() {
        return Transducer.class;
    }

    @Override
    public @NonNull Class<TransducerDao> daoClass() {
        return TransducerDao.class;
    }

    @Override
    public @NonNull JsonTable<TransducerRecord> table() {
        return Tables.TRANSDUCER;
    }

}
