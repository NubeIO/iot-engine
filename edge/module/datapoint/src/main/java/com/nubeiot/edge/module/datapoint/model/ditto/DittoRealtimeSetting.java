package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IRealtimeSetting;

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
