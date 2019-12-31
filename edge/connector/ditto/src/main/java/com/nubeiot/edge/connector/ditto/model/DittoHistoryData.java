package com.nubeiot.edge.connector.ditto.model;

import java.util.Map;
import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.ditto.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IPointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;

import lombok.NonNull;

public final class DittoHistoryData extends AbstractDittoModel<IPointHistoryData> {

    private final Long id;
    private final UUID point;
    private String pointCode;

    public DittoHistoryData(@NonNull PointHistoryData data) {
        super(data);
        this.point = data.getPoint();
        this.id = data.getId();
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
    public JsonObject toJson() {
        return JsonPojo.from(get().setId(null).setPoint(null).setSyncAudit(null)).toJson();
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/histories/properties/" + point + "/" + id;
    }

}
