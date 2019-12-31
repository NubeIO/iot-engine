package com.nubeiot.edge.connector.ditto.model;

import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.INetwork;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public final class DittoNetwork extends AbstractDittoModel<INetwork> {

    public DittoNetwork(@NonNull Network data) {
        super(data);
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/networks/properties" + get().getId();
    }

}
