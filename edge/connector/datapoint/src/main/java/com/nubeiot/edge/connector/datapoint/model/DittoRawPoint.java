package com.nubeiot.edge.connector.datapoint.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.iotdata.model.tables.interfaces.IPoint;
import com.nubeiot.iotdata.model.tables.pojos.Point;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DittoRawPoint implements IDittoModel<IPoint> {

    private final IPoint point;

    @JsonCreator
    public static DittoRawPoint create(@JsonProperty("point") Map<String, Object> settings) {
        return new DittoRawPoint(new Point(JsonData.tryParse(settings).toJson()));
    }

    @Override
    public String jqExpr() {
        return POINT_JQ_EXPR;
    }

    @Override
    public IPoint get() {
        return point;
    }

}
