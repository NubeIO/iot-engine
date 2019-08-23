package com.nubeiot.core.sql.service;

import java.util.Arrays;
import java.util.Collection;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.query.EntityQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;

/**
 * Event Database entity service
 *
 * @param <P> Vertx Pojo
 * @param <M> Metadata type
 * @see EventListener
 * @see VertxPojo
 * @see EntityMetadata
 * @see EntityValidation
 */
//TODO Missing `BATCH` Creation/Modification/Deletion
public interface EntityService<P extends VertxPojo, M extends EntityMetadata> extends EventListener {

    /**
     * Defines {@code CURD} actions
     *
     * @return set of default CRUD action
     */
    @NonNull
    default Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.CREATE, EventAction.UPDATE, EventAction.PATCH, EventAction.REMOVE,
                             EventAction.GET_ONE, EventAction.GET_LIST);
    }

    /**
     * Entity handler
     *
     * @return entity handler
     */
    @NonNull EntityHandler entityHandler();

    /**
     * Query executor to execute {@code CRUD} resource in database layer
     *
     * @return query executor
     * @see EntityQueryExecutor
     */
    @NonNull EntityQueryExecutor<P> queryExecutor();

    /**
     * Context entity metadata
     *
     * @return entity metadata
     * @see EntityMetadata
     */
    @NonNull M context();

    /**
     * Service validation for context resource
     *
     * @return validation
     * @see EntityValidation
     */
    @NonNull EntityValidation validation();

    /**
     * Transformer to convert backend response before pass to client
     *
     * @return transformer
     * @see EntityTransformer
     */
    @NonNull EntityTransformer transformer();

    /**
     * Async post-back service to publish response
     *
     * @return post service
     * @see PostService
     */
    default @NonNull PostService asyncPostService() {
        return PostService.EMPTY;
    }

    /**
     * Defines listener for listing Resource
     *
     * @param requestData Request data
     * @return Json object includes list data
     * @see EventAction#GET_LIST
     */
    Single<JsonObject> list(RequestData requestData);

    /**
     * Defines listener for get one item by key
     *
     * @param requestData Request data
     * @return Json object represents resource data
     * @see EventAction#GET_ONE
     */
    Single<JsonObject> get(RequestData requestData);

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#UPDATE
     */
    Single<JsonObject> create(RequestData requestData);

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#UPDATE
     */
    Single<JsonObject> update(RequestData requestData);

    /**
     * Defines listener for patching existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#PATCH
     */
    Single<JsonObject> patch(RequestData requestData);

    /**
     * Defines listener for deleting existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#REMOVE
     */
    Single<JsonObject> delete(RequestData requestData);

}
