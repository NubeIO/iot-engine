package com.nubeiot.core.sql.query;

import java.util.Optional;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.core.sql.service.marker.GroupReferencingEntityMarker;

import lombok.NonNull;

/**
 * Represents for a {@code sql executor} do {@code DML} or {@code DQL} on {@code group entity}.
 *
 * @param <P>  Type of {@code VertxPojo}
 * @param <CP> Type of {@code CompositePojo}
 * @see GroupReferencingEntityMarker
 * @since 1.0.0
 */
public interface GroupQueryExecutor<P extends VertxPojo, CP extends CompositePojo> extends ReferenceQueryExecutor<CP> {

    /**
     * Create group query executor.
     *
     * @param <K>               Type of {@code primary key}
     * @param <P>               Type of {@code VertxPojo}
     * @param <R>               Type of {@code UpdatableRecord}
     * @param <D>               Type of {@code VertxDAO}
     * @param <CP>              Type of {@code CompositePojo}
     * @param handler           the entity handler
     * @param metadata          the metadata
     * @param compositeMetadata the composite metadata
     * @param marker            the group entity marker
     * @return the group query executor
     * @see EntityHandler
     * @since 1.0.0
     */
    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>,
               CP extends CompositePojo<P, CP>> GroupQueryExecutor<P, CP> create(
        @NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata,
        @NonNull CompositeMetadata<K, P, R, D, CP> compositeMetadata, @NonNull GroupReferencingEntityMarker marker) {
        return new GroupDaoQueryExecutor<>(handler, metadata, compositeMetadata, marker);
    }

    /**
     * @return the group reference marker
     * @see GroupReferencingEntityMarker
     * @since 1.0.0
     */
    @Override
    @NonNull GroupReferencingEntityMarker marker();

    /**
     * Verify {@code entity} whether exists or not.
     *
     * @param reqData the request data
     * @return the single
     * @since 1.0.0
     */
    default Single<Boolean> checkReferenceExistence(@NonNull RequestData reqData) {
        return ReferenceQueryExecutor.super.checkReferenceExistence(reqData)
                                           .concatWith(checkGroupExistence(reqData))
                                           .all(aBoolean -> aBoolean);
    }

    default Single<Boolean> checkGroupExistence(@NonNull RequestData reqData) {
        final EntityReferences references = marker().groupReferences();
        final JsonObject body = reqData.body();
        return Observable.fromIterable(references.getFields().keySet())
                         .flatMapSingle(m -> Optional.ofNullable(body.getJsonObject(m.singularKeyName()))
                                                     .flatMap(b -> Optional.ofNullable(b.getValue(m.jsonKeyName()))
                                                                           .map(Object::toString)
                                                                           .map(m::parseKey))
                                                     .map(k -> fetchExists(queryBuilder().exist(m, k)).switchIfEmpty(
                                                         Single.error(m.notFound(k))))
                                                     .orElseGet(() -> Single.just(true)))
                         .all(aBoolean -> aBoolean);
    }

    @Override
    Single<CP> findOneByKey(RequestData requestData);

}
