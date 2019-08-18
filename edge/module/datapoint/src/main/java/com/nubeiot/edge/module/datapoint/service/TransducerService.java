package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.TransducerMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;

import lombok.NonNull;

public final class TransducerService
    extends AbstractDataPointService<Transducer, TransducerMetadata, TransducerService> {

    public TransducerService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public TransducerMetadata metadata() {
        return TransducerMetadata.INSTANCE;
    }

    @Override
    public TransducerService validation() {
        return this;
    }

}
