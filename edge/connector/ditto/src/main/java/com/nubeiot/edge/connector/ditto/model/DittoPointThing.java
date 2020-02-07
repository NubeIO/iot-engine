package com.nubeiot.edge.connector.ditto.model;

import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.edge.module.datapoint.model.pojos.PointThingComposite;

import lombok.NonNull;

public final class DittoPointThing extends AbstractDittoModel<PointThingComposite> {

    public DittoPointThing(@NonNull PointThingComposite data) {
        super(data);
    }

    @Override
    @NonNull String endpointPattern() {
        return "/things/{0}/features/equipments/properties" + get().getDeviceId();
    }

}
