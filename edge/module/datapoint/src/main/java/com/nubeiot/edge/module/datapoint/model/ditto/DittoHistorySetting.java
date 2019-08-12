package com.nubeiot.edge.module.datapoint.model.ditto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.IHistorySetting;
import com.nubeiot.iotdata.model.tables.pojos.HistorySetting;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class DittoHistorySetting extends AbstractDittoModel<IHistorySetting> {

    private final String pointCode;

    public DittoHistorySetting(String pointCode, IHistorySetting setting) {
        super(setting);
        this.pointCode = Strings.requireNotBlank(pointCode);
    }

    @JsonCreator
    public static DittoHistorySetting create(@JsonProperty("code") String pointCode,
                                             @JsonProperty("historySettings") Map<String, Object> settings) {
        return new DittoHistorySetting(pointCode, new HistorySetting(JsonData.tryParse(settings).toJson()));
    }

    @Override
    public String endpoint(String thingId) {
        return "/things/" + thingId + "/features/points/properties/" + pointCode + "/historySettings";
    }

}
