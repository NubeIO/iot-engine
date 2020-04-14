package com.nubeiot.core.sql.service;

import java.util.Collection;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.decorator.RequestDecorator;
import com.nubeiot.core.sql.query.EntityQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.sql.workflow.task.EntityTaskManager;

import lombok.NonNull;

/**
 * The interface Entity service delegate.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code Entity Metadata}
 * @param <S> Type of {@code Entity Service}
 * @since 1.0.0
 */
public interface EntityServiceDelegate<P extends VertxPojo, M extends EntityMetadata, S extends EntityService<P, M>>
    extends EntityService<P, M> {

    /**
     * Unwrap Entity service.
     *
     * @return the service
     * @since 1.0.0
     */
    S unwrap();

    @Override
    default @NonNull EntityHandler entityHandler() {
        return unwrap().entityHandler();
    }

    @Override
    default M context() {
        return unwrap().context();
    }

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return unwrap().getAvailableEvents();
    }

    @Override
    default @NonNull EntityQueryExecutor<P> queryExecutor() {
        return unwrap().queryExecutor();
    }

    @Override
    default @NonNull RequestDecorator requestDecorator() {
        return unwrap().requestDecorator();
    }

    @Override
    default EntityValidation validation() {
        return unwrap().validation();
    }

    @Override
    default @NonNull EntityTransformer transformer() {
        return unwrap().transformer();
    }

    @Override
    default @NonNull EntityTaskManager taskManager() {
        return unwrap().taskManager();
    }

    @Override
    default boolean supportForceDeletion() {
        return unwrap().supportForceDeletion();
    }

    @Override
    default Single<JsonObject> list(RequestData requestData) {
        return unwrap().list(requestData);
    }

    @Override
    default Single<JsonObject> get(RequestData requestData) {
        return unwrap().get(requestData);
    }

    @Override
    default Single<JsonObject> create(RequestData requestData) {
        return unwrap().create(requestData);
    }

    @Override
    default Single<JsonObject> update(RequestData requestData) {
        return unwrap().update(requestData);
    }

    @Override
    default Single<JsonObject> patch(RequestData requestData) {
        return unwrap().patch(requestData);
    }

    @Override
    default Single<JsonObject> delete(RequestData requestData) {
        return unwrap().delete(requestData);
    }

    @Override
    default Logger logger() {
        return unwrap().logger();
    }

    @Override
    default ObjectMapper mapper() {
        return unwrap().mapper();
    }

    @Override
    default String fallback() {
        return unwrap().fallback();
    }

    @Override
    default Single<EventMessage> apply(Message<Object> message) {
        return unwrap().apply(message);
    }

}
