package com.nubeiot.edge.module.datapoint.model.ditto;

import java.util.Map;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.MeasureUnitMetadata;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel.AbstractDittoModel;
import com.nubeiot.edge.module.datapoint.model.pojos.PointComposite;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;

import lombok.NonNull;

public final class DittoPoint extends AbstractDittoModel<PointComposite> {

    public DittoPoint(@NonNull PointComposite data) {
        super(data);
    }

    @JsonCreator
    public static DittoPoint create(Map<String, Object> point) {
        return new DittoPoint((PointComposite) new PointComposite().fromJson(JsonData.tryParse(point).toJson()));
    }

    @Override
    String endpointPattern() {
        return "/things/{0}/features/points/properties/" + get().getId();
    }

    @Override
    public JsonObject toJson() {
        final PointComposite point = get();
        MeasureUnit other = point.getOther(MeasureUnitMetadata.INSTANCE.singularKeyName());
        other.setSyncAudit(null);
        other.setTimeAudit(null);
        return JsonPojo.from(point.setDevice(null).setMeasureUnit(null).setSyncAudit(null)).toJson();
    }

}
