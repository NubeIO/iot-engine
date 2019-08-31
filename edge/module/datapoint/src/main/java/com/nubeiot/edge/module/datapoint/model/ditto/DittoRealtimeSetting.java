package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IRealtimeSetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;

import lombok.NonNull;

public final class DittoRealtimeSetting extends AbstractDittoModel<IRealtimeSetting> {

    public DittoRealtimeSetting(@NonNull RealtimeSetting data) {
        super(data);
    }

    @Override
    @NonNull String endpointPattern() {
        return "/things/{0}/features/points/properties/" + get().getPoint() + "/realtimeSettings/properties";
    }

}
