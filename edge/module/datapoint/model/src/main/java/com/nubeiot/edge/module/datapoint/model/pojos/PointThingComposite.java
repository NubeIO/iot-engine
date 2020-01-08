package com.nubeiot.edge.module.datapoint.model.pojos;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointThing;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class PointThingComposite extends PointThing implements CompositePojo<PointThing, PointThingComposite> {

    @Getter
    private final ExtensionPojo extension = new ExtensionPojo();

    @Override
    public PointThingComposite wrap(@NonNull PointThing pojo) {
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

}
