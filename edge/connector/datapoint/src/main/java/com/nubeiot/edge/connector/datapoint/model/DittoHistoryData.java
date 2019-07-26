package com.nubeiot.edge.connector.datapoint.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.datapoint.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.IPointHistoryData;
import com.nubeiot.iotdata.model.tables.pojos.PointHistoryData;

import lombok.NonNull;

public final class DittoHistoryData extends AbstractDittoModel<IPointHistoryData> {

    private final String pointCode;

    public DittoHistoryData(String pointCode, @NonNull IPointHistoryData data) {
        super(data);
        this.pointCode = Strings.requireNotBlank(pointCode);
    }

    @JsonCreator
    public static DittoHistoryData create(@JsonProperty("code") String pointCode,
                                          @JsonProperty("data") Map<String, Object> data) {
        return new DittoHistoryData(pointCode, new PointHistoryData(JsonData.tryParse(data).toJson()));
    }

    @Override
    public String endpoint(String thingId) {
        return "/things/" + thingId + "/features/histories/properties/" + pointCode;
    }

}
