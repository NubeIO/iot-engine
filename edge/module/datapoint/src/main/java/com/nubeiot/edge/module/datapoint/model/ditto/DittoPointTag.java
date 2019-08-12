package com.nubeiot.edge.module.datapoint.model.ditto;

import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
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
