package com.nubeiot.core.sql.service;

import java.util.function.Supplier;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.service.EntityPostService.EntitySyncData;
import com.nubeiot.core.transport.ProxyService;
import com.nubeiot.core.transport.Transporter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface EntityPostService<T extends Transporter, D extends EntitySyncData> extends ProxyService<T> {

    EntityPostService EMPTY = new EntityPostService() {

        @Override
        public Transporter transporter() { return null; }

        @Override
        public EntitySyncData transform(@NonNull EntityService service, VertxPojo data) { return null; }

        @Override
        public void onSuccess(@NonNull EntityService service, @NonNull EventAction action, VertxPojo data) { }

        @Override
        public void doSyncOnSuccess(@NonNull EntityService service, @NonNull EventAction action,
                                    EntitySyncData data) { }

        @Override
        public void onError(@NonNull EntityService service, @NonNull EventAction action, @NonNull Throwable t) { }
    };

    @NonNull D transform(@NonNull EntityService service, @NonNull VertxPojo data);

    default void onSuccess(@NonNull EntityService service, @NonNull EventAction action, VertxPojo data) {
        doSyncOnSuccess(service, action, transform(service, data));
    }

    void doSyncOnSuccess(@NonNull EntityService service, @NonNull EventAction action, D data);

    void onError(@NonNull EntityService service, @NonNull EventAction action, @NonNull Throwable throwable);

    interface EntitySyncData<V extends VertxPojo> extends JsonData, Supplier<V> {

        JsonObject body();

    }


    @RequiredArgsConstructor
    abstract class EntityPostServiceDelegate<T extends Transporter, D extends EntitySyncData>
        implements EntityPostService<T, D> {

        @NonNull
        @Getter(value = AccessLevel.PROTECTED)
        private final EntityPostService<T, D> delegate;

        @Override
        public D transform(@NonNull EntityService service, @NonNull VertxPojo data) {
            return delegate.transform(service, data);
        }

        @Override
        public void onSuccess(@NonNull EntityService service, @NonNull EventAction action, VertxPojo data) {
            delegate.onSuccess(service, action, data);
        }

        @Override
        public void doSyncOnSuccess(@NonNull EntityService service, @NonNull EventAction action, D data) {
            delegate.doSyncOnSuccess(service, action, data);
        }

        @Override
        public void onError(@NonNull EntityService service, @NonNull EventAction action, @NonNull Throwable throwable) {
            delegate.onError(service, action, throwable);
        }

        @Override
        public T transporter() {
            return delegate.transporter();
        }

    }

}
