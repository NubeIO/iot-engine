package com.nubeiot.core.sql.service;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.transport.Transporter;

public interface PostService {

    PostService EMPTY = new PostService() {
        @Override
        public Transporter transporter() { return null; }

        @Override
        public void onSuccess(EventAction action, JsonObject json) { }

        @Override
        public void onError(EventAction action, Throwable throwable) { }
    };

    Transporter transporter();

    void onSuccess(EventAction action, JsonObject json);

    void onError(EventAction action, Throwable throwable);

}
