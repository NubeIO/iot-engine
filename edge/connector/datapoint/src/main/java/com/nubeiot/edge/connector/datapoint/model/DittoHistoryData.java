package com.nubeiot.edge.connector.datapoint.model;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.datapoint.model.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.model.tables.interfaces.IPointHistoryData;

import lombok.NonNull;

public final class DittoHistoryData extends AbstractDittoModel<IPointHistoryData> {

    private final String pointCode;

    public DittoHistoryData(String pointCode, @NonNull IPointHistoryData data) {
        super(data);
        this.pointCode = Strings.requireNotBlank(pointCode);
    }

    @Override
    public String endpoint(String thingId) {
        return "/things/" + thingId + "/features/histories/properties/" + pointCode;
    }

}
