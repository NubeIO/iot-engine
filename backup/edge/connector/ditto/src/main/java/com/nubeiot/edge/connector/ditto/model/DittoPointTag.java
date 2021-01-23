package com.nubeiot.edge.connector.ditto.model;

import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IPointTag;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTag;

import lombok.NonNull;

public final class DittoPointTag extends AbstractDittoModel<IPointTag> {

    public DittoPointTag(@NonNull PointTag data) {
        super(data);
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/points/properties/" + get().getPoint() + "/tags";
    }

}
