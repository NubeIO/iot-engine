package com.nubeiot.core.sql.service;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

/**
 * Represents for {@code entity service} in batch mode.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see EntityService
 * @since 1.0.0
 */
public interface BatchEntityService<P extends VertxPojo, M extends EntityMetadata, S extends EntityService<P, M>>
    extends EntityServiceDelegate<P, M, S> {

    @NonNull
    default Collection<EventAction> getAvailableEvents() {
        return Stream.of(unwrap().getAvailableEvents(), ActionMethodMapping.BATCH_DML_MAP.get().keySet())
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    /**
     * Defines listener for updating existing resources in batch
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#BATCH_CREATE
     * @since 1.0.0
     */
    @EventContractor(action = EventAction.BATCH_CREATE, returnType = Single.class)
    Single<JsonObject> batchCreate(@NonNull RequestData requestData);

    /**
     * Defines listener for updating existing resources in batch
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#BATCH_UPDATE
     * @since 1.0.0
     */
    @EventContractor(action = EventAction.BATCH_UPDATE, returnType = Single.class)
    Single<JsonObject> batchUpdate(@NonNull RequestData requestData);

    /**
     * Defines listener for patching existing resources in batch
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#BATCH_PATCH
     * @since 1.0.0
     */
    @EventContractor(action = EventAction.BATCH_PATCH, returnType = Single.class)
    Single<JsonObject> batchPatch(@NonNull RequestData requestData);

    /**
     * Defines listener for deleting existing resources in batch
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#BATCH_DELETE
     * @since 1.0.0
     */
    @EventContractor(action = EventAction.BATCH_DELETE, returnType = Single.class)
    Single<JsonObject> batchDelete(@NonNull RequestData requestData);

}
