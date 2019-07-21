package com.nubeiot.edge.connector.datapoint.model;

import com.nubeiot.edge.connector.datapoint.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.IPointTag;

import lombok.NonNull;

public final class DittoPointTag extends AbstractDittoModel<IPointTag> {

    public DittoPointTag(@NonNull IPointTag data) {
        super(data);
    }

    @Override
    public String endpoint(String thingId) {
        return null;
    }

}
