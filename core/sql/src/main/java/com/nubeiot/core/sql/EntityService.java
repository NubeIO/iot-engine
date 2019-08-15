package com.nubeiot.core.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.sql.type.TimeAudit;

import lombok.NonNull;

/**
 * Event Database entity service
 *
 * @param <K> Entity Primary Key type
 * @param <P> Pojo entity type
 * @param <R> Record type
 * @param <D> DAO type
 * @param <M> Metadata type
 * @see EventListener
 * @see UpdatableRecord
 * @see VertxPojo
 * @see VertxDAO
 */
//TODO Missing `BATCH` Creation/Modification/Deletion
public interface EntityService<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>,
                                  M extends EntityMetadata<K, P, R, D>>
    extends EventListener, Supplier<D> {

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
     * Defines enabling {@code time audit} in {@code application layer} instead of {@code database layer} by {@code DB
     * trigger}. It is helpful to add time audit in {@code create/update/patch} resource.
     *
     * @return {@code true} if enable time audit in application layer
     * @see TimeAudit
     */
    boolean enableTimeAudit();

    /**
     * Enable {@code CUD} response includes full resource instead of simple resource with only response status and
     * {@code primary key} of resource.
     *
     * @return {@code true} if enable full resource in response
     */
    default boolean enableFullResourceInCUDResponse() {
        return true;
    }

    /**
     * Entity handler
     *
     * @return entity handler
     */
    @NonNull EntityHandler entityHandler();

    /**
     * Entity metadata
     *
     * @return entity metadata
     */
    @NonNull M metadata();

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

    /**
     * Parse given data from external service to {@code pojo} object
     *
     * @param request Given request data
     * @return {@code pojo} object resource
     * @throws IllegalArgumentException if cannot parse
     */
    @NonNull
    default P parse(@NonNull JsonObject request) throws IllegalArgumentException {
        return EntityHandler.parse(metadata().modelClass(), request);
    }

    /**
     * Extract primary key from request then parse to primary key with proper data type
     *
     * @param requestData Request data
     * @return Actual primary key
     * @throws IllegalArgumentException if data key is not valid or missing
     */
    @NonNull
    default K parsePrimaryKey(@NonNull RequestData requestData) throws IllegalArgumentException {
        return Optional.ofNullable(requestData.body())
                       .flatMap(body -> Optional.ofNullable(body.getValue(metadata().requestKeyName()))
                                                .map(k -> metadata().parsePrimaryKey(k.toString())))
                       .orElseThrow(() -> new IllegalArgumentException("Missing key " + metadata().requestKeyName()));
    }

}
