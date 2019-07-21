package com.nubeiot.edge.connector.datapoint.model;

import com.nubeiot.edge.connector.datapoint.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.IScheduleSetting;

import lombok.NonNull;

public final class DittoScheduleSetting extends AbstractDittoModel<IScheduleSetting> {

    public DittoScheduleSetting(@NonNull IScheduleSetting data) {
        super(data);
    }

    @Override
    public String endpoint(String thingId) {
        return null;
    }

}
