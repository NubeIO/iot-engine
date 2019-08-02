package com.nubeiot.edge.connector.datapoint.model.ditto;

import com.nubeiot.edge.connector.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.INetwork;

import lombok.NonNull;

public class DittoNetwork extends AbstractDittoModel<INetwork> {

    public DittoNetwork(@NonNull INetwork data) {
        super(data);
    }

    @Override
    public String endpoint(String thingId) {
        return null;
    }

}
