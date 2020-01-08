package com.nubeiot.core.sql.service;

import java.util.Map.Entry;
import java.util.Set;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.SimpleQueryExecutor;
import com.nubeiot.core.sql.service.marker.ReferencedEntityMarker;

import lombok.NonNull;

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
     * Create referenced query executor by given entity metadata.
     *
     * @param <P>               Type of {@code VertxPojo}
     * @param <M>               Type of {@code EntityMetadata}
     * @param dependantMetadata the dependant metadata
     * @return referenced query executor
     * @see SimpleQueryExecutor
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default <P extends VertxPojo, M extends EntityMetadata> SimpleQueryExecutor<P> referencedQuery(
        @NonNull M dependantMetadata) {
        return SimpleQueryExecutor.create(entityHandler(), dependantMetadata);
    }

    default Maybe<CP> onGet(@NonNull CP pojo, @NonNull Object key, @NonNull Set<String> dependants) {
        return dependantEntities().toObservable()
                                  .filter(entry -> dependants.contains(entry.getKey().singularKeyName()))
                                  .flatMapMaybe(entry -> doGet(pojo, key, entry))
                                  .reduce((p1, p2) -> p2);
    }

    @SuppressWarnings("unchecked")
    default MaybeSource<CP> doGet(@NonNull CP pojo, @NonNull Object key, Entry<EntityMetadata, String> entry) {
        final EntityMetadata m = entry.getKey();
        final String rk = entry.getValue();
        return (MaybeSource<CP>) referencedQuery(m).findMany(createRequestData(key, rk))
                                                   .toList()
                                                   .filter(l -> !l.isEmpty())
                                                   .map(l -> pojo.put(m.pluralKeyName(), l));
    }

    default RequestData createRequestData(@NonNull Object key, @NonNull String referenceKey) {
        return RequestData.builder().filter(new JsonObject().put(referenceKey, JsonData.checkAndConvert(key))).build();
    }

}
