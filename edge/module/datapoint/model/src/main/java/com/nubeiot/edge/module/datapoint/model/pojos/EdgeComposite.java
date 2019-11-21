package com.nubeiot.edge.module.datapoint.model.pojos;

import java.util.HashMap;
import java.util.Map;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.iotdata.edge.model.tables.pojos.EdgeDevice;

import lombok.NonNull;

public final class EdgeComposite extends EdgeDevice implements CompositePojo<EdgeDevice, EdgeComposite> {

    private final Map<String, VertxPojo> other = new HashMap<>();

    @Override
    public @NonNull Map<String, VertxPojo> other() {
        return other;
    }

    @Override
    public EdgeComposite wrap(@NonNull EdgeDevice pojo) {
        this.from(pojo);
        return this;
    }

    @Override
    public JsonObject toJson() {
        return super.toJson().mergeIn(otherToJson(), true);
    }

}
