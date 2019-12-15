package com.nubeiot.core.micro.transfomer;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;

import com.nubeiot.core.micro.type.EventMessageService;

import lombok.NonNull;

class PublicRecordTransformer implements RecordTransformer {

    @Override
    public @NonNull JsonObject transform(@NonNull Record record) {
        if (EventMessageService.TYPE.equals(record.getType())) {
            return new EventServiceRecordTransformer().transform(record);
        }
        if (HttpEndpoint.TYPE.equals(record.getType())) {
            return new HttpEndpointRecordTransformer().transform(record);
        }
        return new JsonObject().put("name", record.getName())
                               .put("type", record.getType())
                               .put("status", record.getStatus())
                               .put("location", record.getLocation());
    }

}
