package com.nubeiot.edge.connector.ditto.model;

import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IScheduleSetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.ScheduleSetting;

import lombok.NonNull;

public final class DittoScheduleSetting extends AbstractDittoModel<IScheduleSetting> {

    public DittoScheduleSetting(@NonNull ScheduleSetting data) {
        super(data);
    }

    @Override
    @NonNull String endpointPattern() {
        return "/things/{0}/features/schedules/properties/" + get().getPoint();
    }

}
