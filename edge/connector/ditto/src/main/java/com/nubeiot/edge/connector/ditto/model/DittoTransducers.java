package com.nubeiot.edge.connector.ditto.model;

import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IThing;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;

import lombok.NonNull;

public final class DittoTransducers extends AbstractDittoModel<IThing> {

    public DittoTransducers(@NonNull Thing data) {
        super(data);
    }

    @Override
    @NonNull String endpointPattern() {
        return "/things/{0}/features/transducers/properties" + get().getId();
    }

}
