package com.nubeiot.core.micro.transfomer;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import lombok.NonNull;

class DetailRecordTransformer implements RecordTransformer {

    @Override
    public @NonNull JsonObject transform(@NonNull Record record) {
        return record.toJson();
    }

}
