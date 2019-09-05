package com.nubeiot.core.micro.transfomer;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpLocation;

import lombok.NonNull;

class HttpEndpointRecordTransformer implements RecordTransformer {

    @Override
    public @NonNull JsonObject transform(@NonNull Record record) {
        HttpLocation location = new HttpLocation(record.getLocation());
        return new JsonObject().put("name", record.getName())
                               .put("type", record.getType())
                               .put("status", record.getStatus())
                               .put("location", location.getEndpoint());
    }

}
