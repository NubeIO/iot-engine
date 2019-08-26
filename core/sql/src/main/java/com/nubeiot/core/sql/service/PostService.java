package com.nubeiot.core.sql.service;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.transport.Transporter;

import lombok.NonNull;

public interface PostService {

    PostService EMPTY = new PostService() {
        @Override
        public Transporter transporter() { return null; }

        @Override
        public void onSuccess(@NonNull EntityService service, @NonNull EventAction action, @NonNull JsonObject data) { }

        @Override
        public void onError(@NonNull EntityService service, @NonNull EventAction action, @NonNull Throwable t) { }
    };

    Transporter transporter();

    void onSuccess(@NonNull EntityService service, @NonNull EventAction action, @NonNull JsonObject data);

    void onError(@NonNull EntityService service, @NonNull EventAction action, @NonNull Throwable throwable);

}
