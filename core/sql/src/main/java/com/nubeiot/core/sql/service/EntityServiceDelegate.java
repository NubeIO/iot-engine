package com.nubeiot.core.sql.service;

import java.util.Collection;
import java.util.function.Consumer;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.reactivex.core.eventbus.Message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.query.EntityQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class EntityServiceDelegate<P extends VertxPojo, M extends EntityMetadata, S extends EntityService<P, M>>
    implements EntityService<P, M> {

    @NonNull
    private final S service;

    protected S unwrap() {
        return this.service;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return unwrap().getAvailableEvents();
    }

    @Override
    public @NonNull EntityHandler entityHandler() {
        return unwrap().entityHandler();
    }

    @Override
    public M context() {
        return unwrap().context();
    }

    @Override
    public EntityValidation validation() {
        return unwrap().validation();
    }

    @Override
    public @NonNull EntityQueryExecutor<P> queryExecutor() {
        return unwrap().queryExecutor();
    }

    @Override
    public @NonNull EntityTransformer transformer() {
        return unwrap().transformer();
    }

    @Override
    public @NonNull EntityPostService asyncPostService() {
        return unwrap().asyncPostService();
    }

    @Override
    public Single<JsonObject> list(RequestData requestData) {
        return unwrap().list(requestData);
    }

    @Override
    public Single<JsonObject> get(RequestData requestData) {
        return unwrap().get(requestData);
    }

    @Override
    public Single<JsonObject> create(RequestData requestData) {
        return unwrap().create(requestData);
    }

    @Override
    public Single<JsonObject> update(RequestData requestData) {
        return unwrap().update(requestData);
    }

    @Override
    public Single<JsonObject> patch(RequestData requestData) {
        return unwrap().patch(requestData);
    }

    @Override
    public Single<JsonObject> delete(RequestData requestData) {
        return unwrap().delete(requestData);
    }

    @Override
    public void accept(Message<Object> message) {
        unwrap().accept(message);
    }

    @Override
    public ObjectMapper mapper() {
        return unwrap().mapper();
    }

    @Override
    public String fallback() {
        return unwrap().fallback();
    }

    @Override
    public void accept(io.vertx.core.eventbus.Message<Object> message) {
        unwrap().accept(message);
    }

    @Override
    public Consumer<Throwable> error(io.vertx.core.eventbus.Message<Object> message, EventAction action, Logger logger,
                                     String overrideMsg) {
        return unwrap().error(message, action, logger, overrideMsg);
    }

}
