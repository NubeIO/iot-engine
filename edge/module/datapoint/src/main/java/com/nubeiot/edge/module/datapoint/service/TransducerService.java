package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.TransducerMetadata;
import com.nubeiot.iotdata.edge.model.tables.daos.TransducerDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;
import com.nubeiot.iotdata.edge.model.tables.records.TransducerRecord;

import lombok.NonNull;

public final class TransducerService
    extends AbstractDataPointService<UUID, Transducer, TransducerRecord, TransducerDao, TransducerMetadata> {

    public TransducerService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public TransducerMetadata metadata() {
        return TransducerMetadata.INSTANCE;
    }

}
