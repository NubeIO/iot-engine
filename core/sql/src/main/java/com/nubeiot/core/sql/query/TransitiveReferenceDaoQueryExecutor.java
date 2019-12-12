package com.nubeiot.core.sql.query;

import java.util.AbstractMap.SimpleEntry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.Sort;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.HasReferenceMarker.EntityReferences;
import com.nubeiot.core.sql.service.TransitiveReferenceMarker;
import com.nubeiot.core.sql.service.TransitiveReferenceMarker.TransitiveEntity;

import lombok.NonNull;

@SuppressWarnings("unchecked")
final class TransitiveReferenceDaoQueryExecutor<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>>
    extends SimpleDaoQueryExecutor<K, P, R, D> implements TransitiveReferenceQueryExecutor<P> {

    private final TransitiveReferenceMarker marker;

    TransitiveReferenceDaoQueryExecutor(@NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata,
                                        @NonNull TransitiveReferenceMarker marker) {
        super(handler, metadata);
        this.marker = marker;
    }

    @Override
    public @NonNull TransitiveReferenceMarker marker() {
        return marker;
    }

    @Override
    public Observable<P> findMany(RequestData reqData) {
        final Pagination paging = Optional.ofNullable(reqData.pagination()).orElse(Pagination.builder().build());
        final Function<DSLContext, ResultQuery<R>> viewFunc
            = (Function<DSLContext, ResultQuery<R>>) queryBuilder().view(reqData.filter(), reqData.sort(), paging);
        return mustExists(reqData).flatMapObservable(ignore -> entityHandler().dao(metadata().daoClass())
                                                                              .queryExecutor()
                                                                              .findMany(viewFunc)
                                                                              .flattenAsObservable(rs -> rs));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Single<P> findOneByKey(RequestData reqData) {
        K pk = metadata().parseKey(reqData);
        final JsonObject filter = reqData.filter();
        final Sort sort = reqData.sort();
        return entityHandler().dao(metadata().daoClass())
                              .queryExecutor()
                              .findOne((Function<DSLContext, ResultQuery<R>>) queryBuilder().viewOne(filter, sort))
                              .flatMap(o -> o.map(Single::just).orElse(Single.error(metadata().notFound(pk))))
                              .onErrorResumeNext(EntityQueryExecutor::sneakyThrowDBError);
    }

    @Override
    public Single<Boolean> mustExists(@NonNull RequestData reqData) {
        final EntityReferences references = marker().entityReferences();
        return Observable.fromIterable(references.getFields().entrySet()).flatMapSingle(entry -> {
            final EntityMetadata refMeta = entry.getKey();
            final String refField = entry.getValue();
            final Object key = findKey(reqData, refMeta, refField);
            final TransitiveEntity transitive = marker().transitiveReferences().get(refMeta);
            if (Objects.isNull(key)) {
                if (Objects.nonNull(transitive)) {
                    return Single.error(new IllegalArgumentException("Missing " + refField));
                }
                return Single.just(true);
            }
            if (Objects.isNull(transitive)) {
                return fetchExists(queryBuilder().exist(refMeta, key)).switchIfEmpty(
                    Single.error(refMeta.notFound(key)));
            }
            return checkExistByTransitive(reqData, refMeta, key, transitive);
        }).all(aBoolean -> aBoolean);
    }

    private Single<Boolean> checkExistByTransitive(@NonNull RequestData reqData, @NonNull EntityMetadata refMeta,
                                                   @NonNull Object key, @NonNull TransitiveEntity transitiveEntity) {
        final EntityReferences references = transitiveEntity.getReferences();
        final EntityMetadata context = transitiveEntity.getContext();
        final String refField = context.equals(refMeta) ? refMeta.jsonKeyName() : refMeta.requestKeyName();
        return Observable.fromIterable(references.getFields().entrySet())
                         .flatMap(refEntry -> {
                             final EntityMetadata transitiveMeta = refEntry.getKey();
                             final String transitiveField = refEntry.getValue();
                             final Object transitiveKey = findKey(reqData, transitiveMeta, transitiveField);
                             if (Objects.isNull(transitiveKey)) {
                                 return Observable.empty();
                             }
                             return Observable.just(
                                 new SimpleEntry<>(transitiveField, JsonData.checkAndConvert(transitiveKey)));
                         })
                         .collectInto(new JsonObject(), (o, o2) -> o.put(o2.getKey(), o2.getValue()))
                         .map(json -> json.put(refField, JsonData.checkAndConvert(key)))
                         .flatMapMaybe(filter -> fetchExists(queryBuilder().exist(context, filter)))
                         .switchIfEmpty(Single.error(refMeta.notFound(key)));
    }

    private Object findKey(@NonNull RequestData reqData, @NonNull EntityMetadata meta, @NonNull String refMetaField) {
        return meta.getKey(reqData)
                   .orElse(Optional.ofNullable(reqData.body().getValue(refMetaField))
                                   .map(k -> meta.parseKey(k.toString()))
                                   .orElse(null));
    }

}
