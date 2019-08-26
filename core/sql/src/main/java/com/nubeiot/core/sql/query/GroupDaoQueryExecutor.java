package com.nubeiot.core.sql.query;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SelectJoinStep;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.tables.JsonTable;

import lombok.NonNull;

class GroupDaoQueryExecutor<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>,
                               CP extends CompositePojo<P, CP>>
    extends BaseDaoQueryExecutor<CP> implements GroupQueryExecutor<P, CP> {

    private final CompositeMetadata<K, P, R, D, CP> groupMetadata;

    GroupDaoQueryExecutor(@NonNull EntityHandler handler, @NonNull EntityMetadata<K, P, R, D> metadata,
                          @NonNull CompositeMetadata<K, P, R, D, CP> groupMetadata) {
        super(handler, metadata);
        this.groupMetadata = groupMetadata;
    }

    @Override
    public Observable<CP> findMany(RequestData requestData) {
        return super.findMany(requestData)
                    .map(pojo -> CompositePojo.create(pojo, groupMetadata.rawClass(), groupMetadata.modelClass()));
    }

    @Override
    public Single<CP> findOneByKey(RequestData requestData) {
        return entityHandler().genericQuery()
                              .executeAny(viewOneQuery(requestData.getFilter()))
                              .map(r -> Optional.ofNullable(r.fetchOne(groupMetadata.mapper())))
                              .filter(Optional::isPresent)
                              .switchIfEmpty(Single.error(getMetadata().notFound(getMetadata().parseKey(requestData))))
                              .map(Optional::get)
                              .onErrorResumeNext(EntityQueryExecutor::wrapDatabaseError);
    }

    @Override
    public Single<?> insertReturningPrimary(@NonNull CP pojo, @NonNull RequestData requestData) {
        return null;
    }

    @Override
    public Single<?> modifyReturningPrimary(@NonNull RequestData requestData, @NonNull EventAction action,
                                            BiFunction<VertxPojo, RequestData, VertxPojo> validator) {
        return null;
    }

    @Override
    public Single<CP> deleteOneByKey(RequestData requestData) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Function<DSLContext, ResultQuery<R>> viewQuery(JsonObject filter, Pagination pagination) {
        final @NonNull JsonTable<R> table = groupMetadata.table();
        return context -> {
            final SelectJoinStep<Record> query = context.select(DSL.asterisk()).from(table);
            for (EntityMetadata metadata : groupMetadata.subItems()) {
                join(query, metadata, JoinType.JOIN, filter);
            }
            return (ResultQuery<R>) paging(query.where(condition(table, filter, false)), pagination);
        };
    }

    public Function<DSLContext, ResultQuery<R>> viewOneQuery(JsonObject filter) {
        return viewQuery(filter, Pagination.oneValue());
    }

}
