package com.nubeiot.edge.connector.datapoint.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.iotdata.model.tables.interfaces.IHistorySetting;
import com.nubeiot.iotdata.model.tables.pojos.HistorySetting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class DittoHistorySetting implements IDittoModel<IHistorySetting> {

    private final String pointCode;
    private final IHistorySetting setting;

    DittoHistorySetting() {
        pointCode = null;
        setting = null;
    }

    @JsonCreator
    public static DittoHistorySetting create(@JsonProperty("code") String pointCode,
                                             @JsonProperty("historySettings") Map<String, Object> settings) {
        return new DittoHistorySetting(pointCode, new HistorySetting(JsonData.tryParse(settings).toJson()));
    }

    @Override
    public String jqExpr() {
        return POINT_JQ_EXPR + " | to_entries | map({code: .key, historySettings: .value.historySettings})";
    }

    @Override
    public IHistorySetting get() {
        return setting;
    }

}
