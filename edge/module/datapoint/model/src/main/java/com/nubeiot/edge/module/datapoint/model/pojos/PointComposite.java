package com.nubeiot.edge.module.datapoint.model.pojos;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.MeasureUnitMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class PointComposite extends Point implements CompositePojo<Point, PointComposite> {

    @Getter
    private final ExtensionPojo extension = new ExtensionPojo();

    @Override
    public PointComposite wrap(@NonNull Point pojo) {
        this.from(pojo);
        return this;
    }

    @Override
    public JsonObject toJsonWithoutExt() {
        return super.toJson();
    }

    @Override
    public JsonObject toJson() {
        return super.toJson().mergeIn(extensionToJson(), true);
    }

    public PointComposite addMeasureUnit(MeasureUnit unit) {
        this.put(MeasureUnitMetadata.INSTANCE.singularKeyName(), unit);
        return this;
    }

}
