package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ModelService.UUIDKeyModel;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.TransducersDao;
import com.nubeiot.iotdata.model.tables.pojos.Transducers;
import com.nubeiot.iotdata.model.tables.records.TransducersRecord;

import lombok.NonNull;

public final class TransducerService extends AbstractDittoService<UUID, Transducers, TransducersRecord, TransducersDao>
    implements UUIDKeyModel<Transducers, TransducersRecord, TransducersDao> {

    public TransducerService(TransducersDao dao) {
        super(dao);
    }

    @Override
    protected @NonNull String listKey() {
        return "transducers";
    }

    @Override
    public @NonNull Class<Transducers> model() {
        return Transducers.class;
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
