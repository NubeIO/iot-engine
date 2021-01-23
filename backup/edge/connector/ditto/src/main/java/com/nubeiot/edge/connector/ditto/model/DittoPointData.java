package com.nubeiot.edge.connector.ditto.model;

import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IPointValueData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

import lombok.NonNull;

public final class DittoPointData extends AbstractDittoModel<IPointValueData> {

    public DittoPointData(@NonNull PointValueData data) {
        super(data);
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/points/properties/" + get().getPoint() + "/data";
    }

}
