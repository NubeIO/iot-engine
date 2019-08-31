package com.nubeiot.edge.module.datapoint.model.ditto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IPointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;

import lombok.NonNull;

public final class DittoHistoryData extends AbstractDittoModel<IPointHistoryData> {

    private String pointCode;

    public DittoHistoryData(@NonNull PointHistoryData data) {
        super(data);
    }

    @JsonCreator
    public static DittoHistoryData create(@JsonProperty("code") String pointCode,
                                          @JsonProperty("data") Map<String, Object> data) {
        return new DittoHistoryData(new PointHistoryData(JsonData.tryParse(data).toJson())).pointCode(pointCode);
    }

    private DittoHistoryData pointCode(String pointCode) {
        this.pointCode = Strings.requireNotBlank(pointCode);
        return this;
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/histories/properties/" + get().getPoint();
    }

}
