package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.INetwork;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public final class DittoNetwork extends AbstractDittoModel<INetwork> {

    public DittoNetwork(@NonNull Network data) {
        super(data);
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/networks/properties";
    }

}
