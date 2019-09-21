package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.ITransducer;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;

import lombok.NonNull;

public final class DittoTransducers extends AbstractDittoModel<ITransducer> {

    public DittoTransducers(@NonNull Transducer data) {
        super(data);
    }

    @Override
    @NonNull String endpointPattern() {
        return "/things/{0}/features/transducers/properties" + get().getId();
    }

}
