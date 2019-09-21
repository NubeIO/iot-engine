package com.nubeiot.edge.module.datapoint.model.pojos;

import java.util.HashMap;
import java.util.Map;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.iotdata.edge.model.tables.pojos.DeviceEquip;

import lombok.NonNull;

public final class DeviceComposite extends DeviceEquip implements CompositePojo<DeviceEquip, DeviceComposite> {

    private final Map<String, VertxPojo> other = new HashMap<>();

    @Override
    public @NonNull Map<String, VertxPojo> other() {
        return other;
    }

    @Override
    public DeviceComposite wrap(@NonNull DeviceEquip pojo) {
        this.from(pojo);
        return this;
    }

    @Override
    public JsonObject toJson() {
        return super.toJson().mergeIn(otherToJson(), true);
    }

}
