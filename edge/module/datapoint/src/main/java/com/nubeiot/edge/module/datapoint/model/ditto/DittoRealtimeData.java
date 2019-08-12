package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IPointRealtimeData;

import lombok.NonNull;

public final class DittoRealtimeData extends AbstractDittoModel<IPointRealtimeData> {

    public DittoRealtimeData(@NonNull IPointRealtimeData data) {
        super(data);
    }

    @Override
    public String endpoint(String thingId) {
        return null;
    }

}
