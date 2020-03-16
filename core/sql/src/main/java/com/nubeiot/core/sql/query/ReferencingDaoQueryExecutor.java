package com.nubeiot.core.sql.query;

import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestFilter;
import com.nubeiot.core.dto.Sort;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.service.marker.ReferencingEntityMarker;

import lombok.NonNull;

final class ReferencingDaoQueryExecutor<K, P extends VertxPojo, R extends UpdatableRecord<R>, DAO extends VertxDAO<R, P, K>>
    extends SimpleDaoQueryExecutor<K, P, R, DAO> implements ReferencingQueryExecutor<P> {

    private final ReferencingEntityMarker marker;

    ReferencingDaoQueryExecutor(@NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, DAO> metadata,
                                @NonNull ReferencingEntityMarker marker) {
        super(handler, metadata);
        this.marker = marker;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Single<P> findOneByKey(RequestData reqData) {
        final K pk = metadata().parseKey(reqData);
        final RequestFilter filter = reqData.filter();
        final Sort sort = reqData.sort();
        return dao(metadata()).queryExecutor()
                              .findOne((Function<DSLContext, ResultQuery<R>>) queryBuilder().viewOne(filter, sort))
                              .flatMap(o -> o.map(Single::just).orElse(Single.error(metadata().notFound(pk))))
                              .onErrorResumeNext(EntityQueryExecutor::sneakyThrowDBError);
    }

    @Override
    public @NonNull ReferencingEntityMarker marker() {
        return marker;
    }

}
