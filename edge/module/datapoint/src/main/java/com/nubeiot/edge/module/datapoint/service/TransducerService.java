package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.TransducerMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;

import lombok.NonNull;

public final class TransducerService extends AbstractEntityService<Transducer, TransducerMetadata>
    implements DataPointService<Transducer, TransducerMetadata> {

    public TransducerService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public TransducerMetadata context() {
        return TransducerMetadata.INSTANCE;
    }

}
