package com.nubeiot.edge.connector.ditto.model;

import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.edge.module.datapoint.model.pojos.PointTransducerComposite;

import lombok.NonNull;

public final class DittoPointThing extends AbstractDittoModel<PointTransducerComposite> {

    public DittoPointThing(@NonNull PointTransducerComposite data) {
        super(data);
    }

    @Override
    @NonNull String endpointPattern() {
        return "/things/{0}/features/equipments/properties" + get().getDeviceId();
    }

}
