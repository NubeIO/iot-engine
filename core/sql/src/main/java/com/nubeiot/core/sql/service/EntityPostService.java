package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.transport.ProxyService;
import com.nubeiot.core.transport.Transporter;

import lombok.NonNull;

public interface EntityPostService<T extends Transporter> extends ProxyService<T> {

    EntityPostService EMPTY = new EntityPostService() {

        @Override
        public Transporter transporter() { return null; }

        @Override
        public void onSuccess(@NonNull EntityService service, @NonNull EventAction action, VertxPojo data) { }

        @Override
        public void onError(@NonNull EntityService service, @NonNull EventAction action, @NonNull Throwable t) { }
    };

    void onSuccess(@NonNull EntityService service, @NonNull EventAction action, VertxPojo data);

    void onError(@NonNull EntityService service, @NonNull EventAction action, @NonNull Throwable throwable);

}
