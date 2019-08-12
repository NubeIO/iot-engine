package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.IPointValueData;

import lombok.NonNull;

public final class DittoPointData extends AbstractDittoModel<IPointValueData> {

    public DittoPointData(@NonNull IPointValueData data) {
        super(data);
    }

    @Override
    public String endpoint(String thingId) {
        return null;
    }

}
