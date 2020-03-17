package com.nubeiot.core.sql.service;

import java.util.Objects;
import java.util.function.Function;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.service.marker.OneToOneEntityMarker;

import lombok.NonNull;

/**
 * Represents for an entity service that has {@code one-to-one} relationship to other entities and in business context,
 * it is as {@code parent} entity.
 *
 * @param <CP> Type of {@code CompositePojo}
 * @param <CM> Type of {@code CompositeMetadata}
 * @see ReferencedEntityService
 * @see OneToOneEntityMarker
 * @since 1.0.0
 */
public interface OneToOneParentEntityService<CP extends CompositePojo, CM extends CompositeMetadata>
    extends ReferencedEntityService<CP, CM>, OneToOneEntityMarker {

    default Maybe<CP> get(@NonNull CP pojo, @NonNull JsonObject filter, @NonNull EntityMetadata dependantMetadata) {
        return invoke(pojo, dependantMetadata, EventAction.GET_ONE, RequestData.builder().body(filter).build(),
                      dependantMetadata::parseFromEntity);
    }

    /**
     * Creates list of {@code one-to-one} dependant resources based on the given composite entity
     *
     * @param reqData the request data
     * @param pojo    the composite pojo
     * @param key     the composite pojo's primary key
     * @return composite entity in maybe form
     * @since 1.0.0
     */
    default Maybe<CP> create(@NonNull RequestData reqData, @NonNull CP pojo, @NonNull Object key) {
        return invoke(reqData, pojo, key, EventAction.CREATE);
    }

    /**
     * Updates list of {@code one-to-one} dependant resources based on the given composite entity
     *
     * @param reqData the request data
     * @param pojo    the composite pojo
     * @param key     the composite pojo's primary key
     * @return composite entity in maybe form
     * @since 1.0.0
     */
    default Maybe<CP> update(@NonNull RequestData reqData, @NonNull CP pojo, @NonNull Object key) {
        return invoke(reqData, pojo, key, EventAction.UPDATE);
    }

    /**
     * Patches list of {@code one-to-one} dependant resources based on the given composite entity
     *
     * @param reqData the request data
     * @param pojo    the composite pojo
     * @param key     the composite pojo's primary key
     * @return composite entity in maybe form
     * @since 1.0.0
     */
    default Maybe<CP> patch(@NonNull RequestData reqData, @NonNull CP pojo, @NonNull Object key) {
        return invoke(reqData, pojo, key, EventAction.PATCH);
    }

    /**
     * Invokes the {@code one-to-one} dependant resource service based on the given composite entity
     *
     * @param reqData the req data
     * @param pojo    the pojo
     * @param key     the key
     * @param action  the action
     * @return the maybe
     * @since 1.0.0
     */
    default Maybe<CP> invoke(@NonNull RequestData reqData, @NonNull CP pojo, @NonNull Object key,
                             @NonNull EventAction action) {
        final Object pk = JsonData.checkAndConvert(key);
        return dependantEntities().toObservable()
                                  .flatMapMaybe(
                                      en -> invoke(reqData, pojo, new JsonObject().put(en.getValue(), pk), en.getKey(),
                                                   action))
                                  .reduce((p1, p2) -> p2)
                                  .defaultIfEmpty(pojo);
    }

    /**
     * Invokes the {@code one-to-one} dependant resource service based on the given composite entity
     *
     * @param requestData       the request data
     * @param pojo              the composite pojo
     * @param body              the body includes composite pojo key in dependant resource
     * @param dependantMetadata the dependant entity metadata
     * @param action            the request action
     * @return composite entity in maybe form
     * @since 1.0.0
     */
    default Maybe<CP> invoke(@NonNull RequestData requestData, @NonNull CP pojo, @NonNull JsonObject body,
                             @NonNull EntityMetadata dependantMetadata, @NonNull EventAction action) {
        final JsonObject dependant = requestData.body().getJsonObject(dependantMetadata.singularKeyName());
        if (Objects.isNull(dependant)) {
            return Maybe.just(pojo);
        }
        final JsonObject reqBody = dependant.mergeIn(body);
        final RequestData reqData = RequestData.builder().body(reqBody).headers(requestData.headers()).build();
        return invoke(pojo, dependantMetadata, action, reqData,
                      json -> dependantMetadata.parseFromEntity(json.getJsonObject("resource", new JsonObject())));
    }

    /**
     * Invoke the dependant service then parsing and handling error.
     *
     * @param pojo              the composite pojo
     * @param dependantMetadata the dependant metadata
     * @param action            the action
     * @param requestData       the request data
     * @param parser            the response parser
     * @return composite entity in maybe form
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default Maybe<CP> invoke(@NonNull CP pojo, @NonNull EntityMetadata dependantMetadata, @NonNull EventAction action,
                             @NonNull RequestData requestData, @NonNull Function<JsonObject, VertxPojo> parser) {
        return invoke(dependantMetadata, action, requestData).map(parser::apply)
                                                             .map(r -> pojo.put(dependantMetadata.singularKeyName(), r))
                                                             .onErrorReturn(t -> pojo)
                                                             .map(p -> (CP) p);
    }

}
