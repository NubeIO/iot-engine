package com.nubeiot.core.sql.service;

import java.util.Collection;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestFilter;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.decorator.RequestDecorator;
import com.nubeiot.core.sql.query.EntityQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.sql.workflow.task.EntityTaskManager;

import lombok.NonNull;

/**
 * Represents for an {@code entity service} based on {@code eventbus listener}
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see EventListener
 * @see VertxPojo
 * @see EntityMetadata
 * @since 1.0.0
 */
//TODO Missing `BATCH` Creation/Modification/Deletion
public interface EntityService<P extends VertxPojo, M extends EntityMetadata>
    extends EventListener, BaseEntityService<M> {

    /**
     * Defines {@code CURD} actions
     *
     * @return set of default CRUD action
     * @since 1.0.0
     */
    @NonNull
    default Collection<EventAction> getAvailableEvents() {
        return ActionMethodMapping.CRUD_MAP.get().keySet();
    }

    /**
     * Query executor to execute {@code CRUD} resource in database layer
     *
     * @return query executor
     * @see EntityQueryExecutor
     * @since 1.0.0
     */
    @NonNull EntityQueryExecutor<P> queryExecutor();

    /**
     * Helps decorating request before handling request
     *
     * @return request decorator
     * @see RequestDecorator
     * @since 1.0.0
     */
    @NonNull RequestDecorator requestDecorator();

    /**
     * Service validation for context resource
     *
     * @return validation entity validation
     * @see EntityValidation
     * @since 1.0.0
     */
    @NonNull EntityValidation validation();

    /**
     * Transformer to convert backend response before pass to client
     *
     * @return transformer entity transformer
     * @see EntityTransformer
     * @since 1.0.0
     */
    @NonNull EntityTransformer transformer();

    /**
     * Defines {@code Entity Task manager}.
     *
     * @return the task manager
     * @since 1.0.0
     */
    default @NonNull EntityTaskManager taskManager() {
        return EntityTaskManager.EMPTY;
    }

    /**
     * Defines supporting {@code force deletion}.
     *
     * @return {@code true} if supported. Default is {@code false}
     * @see RequestFilter#hasForce()
     * @since 1.0.0
     */
    default boolean supportForceDeletion() {
        return false;
    }

    /**
     * Defines listener for listing Resource
     *
     * @param requestData Request data
     * @return Json object includes list data
     * @see EventAction#GET_LIST
     * @since 1.0.0
     */
    Single<JsonObject> list(RequestData requestData);

    /**
     * Defines listener for get one item by key
     *
     * @param requestData Request data
     * @return Json object represents resource data
     * @see EventAction#GET_ONE
     * @since 1.0.0
     */
    Single<JsonObject> get(RequestData requestData);

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#CREATE
     * @since 1.0.0
     */
    Single<JsonObject> create(RequestData requestData);

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#UPDATE
     * @since 1.0.0
     */
    Single<JsonObject> update(RequestData requestData);

    /**
     * Defines listener for patching existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#PATCH
     * @since 1.0.0
     */
    Single<JsonObject> patch(RequestData requestData);

    /**
     * Defines listener for deleting existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#REMOVE
     * @since 1.0.0
     */
    Single<JsonObject> delete(RequestData requestData);

}
