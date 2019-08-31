package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IPointRealtimeData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;

import lombok.NonNull;

public final class DittoRealtimeData extends AbstractDittoModel<IPointRealtimeData> {

    public DittoRealtimeData(@NonNull PointRealtimeData data) {
        super(data);
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/realtime/properties/" + get().getPoint();
    }

}
