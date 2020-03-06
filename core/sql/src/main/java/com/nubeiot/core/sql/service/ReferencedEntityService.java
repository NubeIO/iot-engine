package com.nubeiot.core.sql.service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.Maybe;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.component.EventClientProxy;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.service.marker.ReferencedEntityMarker;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

/**
 * Represents for an entity service that holds a {@code resource entity} is referenced by other resources.
 *
 * @param <CP> Type of {@code CompositePojo}
 * @param <CM> Type of {@code CompositeMetadata}
 * @see ReferencedEntityMarker
 * @since 1.0.0
 */
public interface ReferencedEntityService<CP extends CompositePojo, CM extends CompositeMetadata>
    extends BaseEntityService<CM>, ReferencedEntityMarker {

    /**
     * Declares the dependant services map between {@code EntityMetadata} and {@code service address}
     *
     * @return the map
     * @since 1.0.0
     */
    @NonNull Map<EntityMetadata, ReferenceServiceMetadata> dependantServices();

    /**
     * Gets logger.
     *
     * @return the logger
     * @since 1.0.0
     */
    default @NonNull Logger logger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Proxy client event client proxy.
     *
     * @return the event client proxy
     * @since 1.0.0
     */
    default @NonNull EventClientProxy proxyClient() {
        return () -> entityHandler().eventClient();
    }

    /**
     * Queries list of dependant resources based on the given composite entity and the given dependant resource names
     *
     * @param pojo       the given composite entity
     * @param key        the given composite entity's primary key that will be used to make query filter on dependant
     *                   resource
     * @param dependants the given list of dependant name
     * @return composite entity in maybe form
     * @apiNote the given dependant resource names will be intersected by the definition reference resource {@link
     *     #dependantEntities()}
     * @see #get(CompositePojo, JsonObject, EntityMetadata)
     * @since 1.0.0
     */
    default Maybe<CP> get(@NonNull CP pojo, @NonNull Object key, @NonNull Set<String> dependants) {
        final Object pk = JsonData.checkAndConvert(key);
        return dependantEntities().toObservable()
                                  .filter(entry -> dependants.contains(entry.getKey().singularKeyName()))
                                  .flatMapMaybe(en -> get(pojo, new JsonObject().put(en.getValue(), pk), en.getKey()))
                                  .reduce((p1, p2) -> p2)
                                  .defaultIfEmpty(pojo);
    }

    /**
     * Queries the reference resource based on the given composite entity and the given dependant resource
     *
     * @param pojo              the given composite entity
     * @param filter            the given filter on dependant resource
     * @param dependantMetadata the given dependant metadata
     * @return composite entity in maybe form
     * @see #get(CompositePojo, Object, Set)
     * @see EntityMetadata
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default Maybe<CP> get(@NonNull CP pojo, @NonNull JsonObject filter, @NonNull EntityMetadata dependantMetadata) {
        final String refKeyName = dependantMetadata.pluralKeyName();
        final RequestData reqData = RequestData.builder().filter(filter).build();
        final Maybe<JsonObject> invoker = invoke(dependantMetadata, EventAction.GET_LIST, reqData);
        return invoker.map(d -> d.getJsonArray(refKeyName, new JsonArray()))
                      .filter(l -> !l.isEmpty())
                      .map(l -> l.stream()
                                 .map(o -> dependantMetadata.parseFromEntity((JsonObject) o))
                                 .collect(Collectors.toList()))
                      .map(l -> pojo.put(refKeyName, l))
                      .map(p -> (CP) p);
    }

    /**
     * Invoke the dependant service then parsing and handling response data.
     *
     * @param dependantMetadata the dependant metadata
     * @param action            the request action
     * @param requestData       the request data
     * @return json response in maybe form
     * @see #proxyClient()
     * @see #dependantServices()
     * @since 1.0.0
     */
    default Maybe<JsonObject> invoke(@NonNull EntityMetadata dependantMetadata, @NonNull EventAction action,
                                     @NonNull RequestData requestData) {
        final ReferenceServiceMetadata referenceServiceMetadata = dependantServices().get(dependantMetadata);
        if (Objects.isNull(referenceServiceMetadata)) {
            logger().debug("Unspecified the dependant service address '{}' of '{}'",
                           dependantMetadata.modelClass().getName(), context().modelClass().getName());
            return Maybe.empty();
        }
        final EventMessage reqMsg = EventMessage.initial(referenceServiceMetadata.findEventAction(action), requestData);
        return proxyClient().transporter()
                            .request(referenceServiceMetadata.getAddress(), reqMsg)
                            .doOnError(
                                t -> logger().warn("Referenced entity service error when invoke {} in address {}", t,
                                                   reqMsg.getAction(), referenceServiceMetadata))
                            .map(EventMessage::getData)
                            .toMaybe();
    }

    @Builder
    class ReferenceServiceMetadata {

        @NonNull
        @Getter
        private final String address;
        @Singular
        @NonNull
        private Map<EventAction, EventAction> actions;

        public @NonNull EventAction findEventAction(@NonNull EventAction action) {
            return actions.getOrDefault(action, action);
        }

    }

}
