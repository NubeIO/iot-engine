package com.nubeiot.edge.connector.datapoint.model;

import com.nubeiot.edge.connector.datapoint.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.IRealtimeSetting;

import lombok.NonNull;

public final class DittoRealtimeSetting extends AbstractDittoModel<IRealtimeSetting> {

    public DittoRealtimeSetting(@NonNull IRealtimeSetting data) {
        super(data);
    }

    @Override
    public String endpoint(String thingId) {
        return null;
    }

}
