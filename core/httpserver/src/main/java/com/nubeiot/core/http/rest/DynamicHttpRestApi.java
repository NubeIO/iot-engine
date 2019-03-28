package com.nubeiot.core.http.rest;

import java.util.Optional;
import java.util.Set;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.base.Urls;

import lombok.NonNull;

public interface DynamicHttpRestApi extends DynamicRestApi {

    static DynamicHttpRestApi create(@NonNull Record record) {
        HttpLocation location = record.getLocation().mapTo(HttpLocation.class);
        return new DynamicHttpRestApi() {
            @Override
            public String path() {
                return Urls.combinePath(location.getRoot(), ApiConstants.WILDCARDS_ANY_PATH);
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
    default Optional<Set<String>> alternativePaths() { return Optional.empty(); }

    @Override
    default String type() { return HttpEndpoint.TYPE; }

}
