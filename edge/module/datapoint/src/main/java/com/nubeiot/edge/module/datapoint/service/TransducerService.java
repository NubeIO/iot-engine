package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.edge.module.datapoint.service.Metadata.TransducerMetadata;

import lombok.NonNull;

public final class TransducerService extends AbstractDataPointService<TransducerMetadata, TransducerService> {

    public TransducerService(@NonNull AbstractEntityHandler entityHandler) {
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
