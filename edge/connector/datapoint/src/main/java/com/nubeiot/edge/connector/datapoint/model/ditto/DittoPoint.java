package com.nubeiot.edge.connector.datapoint.model.ditto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.edge.connector.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.IPoint;
import com.nubeiot.iotdata.model.tables.pojos.Point;

public final class DittoPoint extends AbstractDittoModel<IPoint> {

    public DittoPoint(IPoint data) {
        super(data);
    }

    @JsonCreator
    public static DittoPoint create(Map<String, Object> point) {
        return new DittoPoint(new Point(JsonData.tryParse(point).toJson()));
    }

    @Override
    public String endpoint(String thingId) {
        return "/things/" + thingId + "/features/points/properties/" + get().getCode();
    }

}
