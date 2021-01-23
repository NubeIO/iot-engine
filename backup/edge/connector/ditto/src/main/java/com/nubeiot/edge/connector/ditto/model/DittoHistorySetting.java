package com.nubeiot.edge.connector.ditto.model;

import java.util.Map;

import io.github.zero88.utils.Strings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IHistorySetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class DittoHistorySetting extends AbstractDittoModel<IHistorySetting> {

    private String pointCode;

    public DittoHistorySetting(HistorySetting setting) {
        super(setting);
    }

    @JsonCreator
    public static DittoHistorySetting create(@JsonProperty("code") String pointCode,
                                             @JsonProperty("historySettings") Map<String, Object> settings) {
        return new DittoHistorySetting(new HistorySetting(JsonData.tryParse(settings).toJson())).pointCode(pointCode);
    }

    private DittoHistorySetting pointCode(String pointCode) {
        this.pointCode = Strings.requireNotBlank(pointCode);
        return this;
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/points/properties/" + get().getPoint() + "/historySettings";
    }

}
