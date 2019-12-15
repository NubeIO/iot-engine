package com.nubeiot.core.sql.service;

import java.util.function.Supplier;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
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
        public @NonNull Maybe transform(@NonNull EntityService service, VertxPojo data) { return Maybe.empty(); }

        @Override
        public void onSuccess(@NonNull EntityService service, @NonNull EventAction action, VertxPojo data,
                              @NonNull RequestData requestData) { }

        @Override
        public Maybe<JsonObject> doSyncOnSuccess(@NonNull EntityService service, @NonNull EventAction action,
                                                 @NonNull EntitySyncData data, @NonNull RequestData requestData) {
            return Maybe.empty();
        }

        @Override
        public void onError(@NonNull EntityService service, @NonNull EventAction action, @NonNull Throwable t) { }
    };

    @NonNull Maybe<D> transform(@NonNull EntityService service, @NonNull VertxPojo data);

    default void onSuccess(@NonNull EntityService service, @NonNull EventAction action, @NonNull VertxPojo data,
                           @NonNull RequestData requestData) {
        transform(service, data).map(syncData -> doSyncOnSuccess(service, action, syncData, requestData)).subscribe();
    }

    Maybe<JsonObject> doSyncOnSuccess(@NonNull EntityService service, @NonNull EventAction action, @NonNull D data,
                                      @NonNull RequestData requestData);

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
        public @NonNull Maybe<D> transform(@NonNull EntityService service, @NonNull VertxPojo data) {
            return delegate.transform(service, data);
        }

        @Override
        public void onSuccess(@NonNull EntityService service, @NonNull EventAction action, @NonNull VertxPojo data,
                              @NonNull RequestData requestData) {
            delegate.onSuccess(service, action, data, requestData);
        }

        @Override
        public Maybe<JsonObject> doSyncOnSuccess(@NonNull EntityService service, @NonNull EventAction action,
                                                 @NonNull D data, @NonNull RequestData requestData) {
            return delegate.doSyncOnSuccess(service, action, data, requestData);
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
