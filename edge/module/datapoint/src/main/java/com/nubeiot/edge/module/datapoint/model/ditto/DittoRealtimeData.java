package com.nubeiot.edge.module.datapoint.model.ditto;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.iotdata.edge.model.tables.interfaces.IPointRealtimeData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;

import lombok.NonNull;

public final class DittoRealtimeData extends AbstractDittoModel<IPointRealtimeData> {

    private final Long id;
    private final UUID point;

    public DittoRealtimeData(@NonNull PointRealtimeData data) {
        super(data);
        this.point = data.getPoint();
        this.id = data.getId();
    }

    @Override
    public JsonObject body() {
        return JsonPojo.from(get().setId(null).setPoint(null).setSyncAudit(null)).toJson();
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/realtime/properties/" + point + "/" + id;
    }

}
