package com.nubeiot.edge.connector.datapoint.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.functions.Consumer;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.client.HttpClientDelegate;

import lombok.RequiredArgsConstructor;

public interface Publisher extends Consumer<JsonObject> {

    static <T extends VertxPojo> Publisher create(Class<T> clazz) {
        return null;
    }

    @RequiredArgsConstructor
    class RestPublisher implements Publisher {

        private final HttpClientDelegate delegate;

        @Override
        public void accept(JsonObject entries) throws Exception {

        }

    }

}
