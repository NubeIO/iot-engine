package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.ITransducer;

import lombok.NonNull;

public final class DittoTransducers extends AbstractDittoModel<ITransducer> {

    public DittoTransducers(@NonNull ITransducer data) {
        super(data);
    }

    @Override
    public String endpoint(String thingId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
