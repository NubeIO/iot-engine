package com.nubeiot.edge.module.datapoint.model.ditto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IPoint;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

import lombok.NonNull;

public final class DittoPoint extends AbstractDittoModel<IPoint> {

    public DittoPoint(@NonNull Point data) {
        super(data);
    }

    @JsonCreator
    public static DittoPoint create(Map<String, Object> point) {
        return new DittoPoint(new Point(JsonData.tryParse(point).toJson()));
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/points/properties/" + get().getCode();
    }

}
