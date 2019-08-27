package com.nubeiot.core.sql.query;

import java.util.Objects;
import java.util.Optional;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.service.GroupReferenceResource;
import com.nubeiot.core.sql.service.HasReferenceResource.EntityReferences;

import lombok.NonNull;

public interface GroupQueryExecutor<P extends VertxPojo, CP extends CompositePojo> extends ReferenceQueryExecutor<CP> {

    static <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>,
               CP extends CompositePojo<P, CP>> GroupQueryExecutor<P, CP> create(
        @NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata,
        @NonNull CompositeMetadata<K, P, R, D, CP> compositeMetadata) {
        return new GroupDaoQueryExecutor<>(handler, metadata, compositeMetadata);
    }

    @Override
    Single<CP> findOneByKey(RequestData requestData);

    default Maybe<Boolean> mustExists(@NonNull RequestData reqData, @NonNull GroupReferenceResource groupRef) {
        final EntityReferences references = groupRef.groupReferences();
        final JsonObject body = reqData.body();
        Maybe<Boolean> ref = ReferenceQueryExecutor.super.mustExists(reqData, groupRef);
        Maybe<Boolean> group = Observable.fromIterable(references.getFields().entrySet())
                                         .filter(Objects::nonNull)
                                         .flatMapMaybe(entry -> {
                                             EntityMetadata m = entry.getKey();
                                             Optional key = Optional.ofNullable(body.getJsonObject(m.singularKeyName()))
                                                                    .flatMap(b -> Optional.ofNullable(
                                                                        b.getValue(m.jsonKeyName()))
                                                                                          .map(Object::toString)
                                                                                          .map(m::parseKey));
                                             if (!key.isPresent()) {
                                                 return Maybe.just(true);
                                             }
                                             return fetchExists(existQuery(m, key.get())).switchIfEmpty(
                                                 Maybe.error(m.notFound(key.get())));
                                         })
                                         .reduce((b1, b2) -> b1 && b2);
        return ref.zipWith(group, (b1, b2) -> b1 && b2);
    }

}
