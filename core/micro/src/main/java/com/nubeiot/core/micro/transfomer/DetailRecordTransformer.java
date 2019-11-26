package com.nubeiot.core.micro.transfomer;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.micro.type.EventMessageService;

import lombok.NonNull;

class DetailRecordTransformer implements RecordTransformer {

    @Override
    public @NonNull JsonObject transform(@NonNull Record record) {
        if (EventMessageService.TYPE.equals(record.getType())) {
            return new EventServiceTechnicalRecordTransformer().transform(record);
        }
        return record.toJson();
    }

}
