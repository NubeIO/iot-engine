package com.nubeiot.core.micro.filter;

import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.http.base.Urls;

import lombok.NonNull;

final class HttpEndpointPathPredicate implements ByPathPredicate {

    @Override
    public boolean test(@NonNull Record record, @NonNull String path) {
        HttpLocation location = new HttpLocation(record.getLocation());
        return location.getRoot().equals(Urls.combinePath(path));
    }

}
