package com.nubeiot.core.sql.service;

import java.util.Collection;
import java.util.Optional;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.query.EntityQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.sql.workflow.task.EntityTask;

import lombok.NonNull;

/**
 * Represents for {@code entity service} based on {@code eventbus listener}
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see EventListener
 * @see VertxPojo
 * @see EntityMetadata
 * @see EntityValidation
 * @see EntityTransformer
 * @since 1.0.0
 */
//TODO Missing `BATCH` Creation/Modification/Deletion
public interface EntityService<P extends VertxPojo, M extends EntityMetadata>
    extends EventListener, BaseEntityService<M> {

    /**
     * Defines {@code CURD} actions
     *
     * @return set of default CRUD action
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
     * Defines {@code blocking pre-task} is run before the entity manager do query or persist
     *
     * @return pre blocking task
     * @see EntityTask
     * @since 1.0.0
     */
    default Optional<? extends EntityTask> prePersistTask() {
        return Optional.empty();
    }

    /**
     * Defines {@code blocking post-task} is run after the entity manager do query or persist
     *
     * @return post blocking task
     * @see EntityTask
     * @since 1.0.0
     */
    default Optional<? extends EntityTask> postPersistTask() {
        return Optional.empty();
    }

    /**
     * Defines {@code async post-task} is run after the entity manager do query or persist
     *
     * @return post async task
     * @see EntityTask
     * @since 1.0.0
     */
    default Optional<? extends EntityTask> postPersistAsyncTask() {
        return Optional.empty();
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
