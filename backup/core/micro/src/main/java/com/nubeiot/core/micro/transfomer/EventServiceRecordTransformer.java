package com.nubeiot.core.micro.transfomer;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.base.event.EventMethodMapping;
import com.nubeiot.core.micro.type.EventMessageService;

import lombok.NonNull;

class EventServiceRecordTransformer implements RecordTransformer {

    @Override
    public @NonNull RecordOutput transform(@NonNull Record record) {
        EventMethodDefinition definition = JsonData.convert(
            record.getMetadata().getJsonObject(EventMessageService.EVENT_METHOD_CONFIG), EventMethodDefinition.class);
        final JsonArray paths = definition.getMapping()
                                          .stream()
                                          .map(this::serializeEventMethod)
                                          .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
        return RecordOutput.builder()
                           .name(record.getName())
                           .status(record.getStatus())
                           .location(record.getLocation().getString(Record.ENDPOINT))
                           .endpoints(paths)
                           .build();
    }

    protected JsonObject serializeEventMethod(@NonNull EventMethodMapping map) {
        return new JsonObject().put("method", map.getMethod()).put("path", map.getCapturePath());
    }

}
