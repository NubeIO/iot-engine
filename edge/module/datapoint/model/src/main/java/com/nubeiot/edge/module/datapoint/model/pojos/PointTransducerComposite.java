package com.nubeiot.edge.module.datapoint.model.pojos;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTransducer;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class PointTransducerComposite extends PointTransducer
    implements CompositePojo<PointTransducer, PointTransducerComposite> {

    @Getter
    private final ExtensionPojo extension = new ExtensionPojo();

    @Override
    public PointTransducerComposite wrap(@NonNull PointTransducer pojo) {
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
