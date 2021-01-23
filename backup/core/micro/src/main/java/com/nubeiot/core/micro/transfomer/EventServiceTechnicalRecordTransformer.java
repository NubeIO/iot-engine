package com.nubeiot.core.micro.transfomer;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.base.event.EventMethodMapping;

import lombok.NonNull;

class EventServiceTechnicalRecordTransformer extends EventServiceRecordTransformer implements RecordTransformer {

    @Override
    protected JsonObject serializeEventMethod(@NonNull EventMethodMapping map) {
        return map.toJson();
    }

}
