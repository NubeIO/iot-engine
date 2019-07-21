package com.nubeiot.edge.connector.datapoint.model;

import com.nubeiot.edge.connector.datapoint.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.ITransducers;

import lombok.NonNull;

public final class DittoTransducers extends AbstractDittoModel<ITransducers> {

    public DittoTransducers(@NonNull ITransducers data) {
        super(data);
    }

    @Override
    public String endpoint(String thingId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
