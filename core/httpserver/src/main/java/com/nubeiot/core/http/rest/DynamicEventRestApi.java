package com.nubeiot.core.http.rest;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.micro.type.EventMessageService;

import lombok.NonNull;

public interface DynamicEventRestApi extends DynamicRestApi {

    static DynamicEventRestApi create(Record record) {
        return new DynamicEventRestApi() {
            @Override
            public @NonNull String path() {
                return record.getLocation().getString("endpoint");
            }

            @Override
            public @NonNull String name() {
                return record.getName();
            }

            @Override
            public JsonObject byMetadata() {
                return record.getMetadata();
            }
        };
    }

    @Override
    default String type() {
        return EventMessageService.TYPE;
    }

}
