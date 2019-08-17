package com.nubeiot.core.sql.service;

import java.util.Arrays;
import java.util.Collection;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.validation.EntityValidation;

import lombok.NonNull;

/**
 * Event Database entity service
 *
 * @param <M> Metadata type
 * @see EventListener
 * @see UpdatableRecord
 * @see VertxPojo
 * @see VertxDAO
 */
//TODO Missing `BATCH` Creation/Modification/Deletion
public interface EntityService<M extends EntityMetadata, V extends EntityValidation> extends EventListener {

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
    @NonNull AbstractEntityHandler entityHandler();

    /**
     * Entity metadata
     *
     * @return entity metadata
     */
    @NonNull M metadata();

    @NonNull V validation();

    @NonNull EntityTransformer transformer();

    default @NonNull PostService postAction() {
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
