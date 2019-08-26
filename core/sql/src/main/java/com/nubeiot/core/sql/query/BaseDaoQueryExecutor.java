package com.nubeiot.core.sql.query;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jooq.DSLContext;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
abstract class BaseDaoQueryExecutor<P extends VertxPojo> implements EntityQueryExecutor<P> {

    private final EntityHandler handler;
    private final EntityMetadata metadata;

    @Override
    public final EntityHandler entityHandler() {
        return handler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Observable<P> findMany(RequestData reqData) {
        final Pagination paging = Optional.ofNullable(reqData.getPagination()).orElse(Pagination.builder().build());
        return ((Single<List<P>>) entityHandler().dao(metadata.daoClass())
                                                 .queryExecutor()
                                                 .findMany(viewQuery(reqData.getFilter(), paging))).flattenAsObservable(
            records -> records);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Maybe<P> lookupByPrimaryKey(@NonNull Object primaryKey) {
        final Single<Optional<P>> oneById = (Single<Optional<P>>) handler.dao(metadata.daoClass())
                                                                         .findOneById(primaryKey);
        return oneById.flatMapMaybe(o -> o.map(Maybe::just).orElse(Maybe.error(metadata.notFound(primaryKey))));
    }

    @Override
    public final <X> Single<X> executeAny(Function<DSLContext, X> function) {
        return entityHandler().genericQuery().executeAny(function);
    }

}
