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
    public QueryBuilder queryBuilder() {
        return new QueryBuilder(groupMetadata).references(groupMetadata.subItems());
    }

    @Override
    public Observable<CP> findMany(RequestData requestData) {
        return super.findMany(requestData)
                    .map(pojo -> CompositePojo.create(pojo, groupMetadata.rawClass(), groupMetadata.modelClass()));
    }

    @Override
    public Single<CP> findOneByKey(RequestData requestData) {
        return entityHandler().genericQuery().executeAny(queryBuilder().viewOne(requestData.getFilter()))
                              .map(r -> Optional.ofNullable(r.fetchOne(groupMetadata.mapper())))
                              .filter(Optional::isPresent)
                              .switchIfEmpty(Single.error(getMetadata().notFound(getMetadata().parseKey(requestData))))
                              .map(Optional::get)
                              .onErrorResumeNext(EntityQueryExecutor::wrapDatabaseError);
    }

    @Override
    public Single<CP> lookupByPrimaryKey(@NonNull Object primaryKey) {
        final JsonObject filter = new JsonObject().put(getMetadata().requestKeyName(), primaryKey.toString())
                                                  .put(getMetadata().jsonKeyName(), primaryKey.toString());
        return findOneByKey(RequestData.builder().body(filter).filter(filter).build());
    }

}
