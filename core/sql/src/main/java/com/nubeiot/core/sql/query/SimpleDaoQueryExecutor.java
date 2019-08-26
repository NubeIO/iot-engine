package com.nubeiot.core.sql.query;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

@SuppressWarnings("unchecked")
class SimpleDaoQueryExecutor<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>>
    extends BaseDaoQueryExecutor<P> implements SimpleQueryExecutor<P> {

    SimpleDaoQueryExecutor(EntityHandler handler, EntityMetadata<K, P, R, D> metadata) {
        super(handler, metadata);
    }

    protected EntityMetadata<K, P, R, D> metadata() {
        return super.getMetadata();
    }

    @Override
    public Observable<P> findMany(RequestData reqData) {
        final Pagination paging = Optional.ofNullable(reqData.getPagination()).orElse(Pagination.builder().build());
        return entityHandler().dao(metadata().daoClass())
                              .queryExecutor()
                              .findMany(viewQuery(reqData.getFilter(), paging))
                              .flattenAsObservable(records -> records);
    }

    @Override
    public Single<P> findOneByKey(RequestData requestData) {
        K pk = metadata().parseKey(requestData);
        return entityHandler().dao(metadata().daoClass())
                              .findOneById(pk)
                              .flatMap(o -> o.map(Single::just).orElse(Single.error(metadata().notFound(pk))));
    }

    @Override
    public Single<K> insertReturningPrimary(VertxPojo pojo, RequestData reqData) {
        final @NonNull Optional<K> opt = metadata().getKey(reqData);
        final D dao = entityHandler().dao(metadata().daoClass());
        return opt.map(key -> fetchExists(existQuery(metadata(), key)).map(b -> key))
                  .orElse(Maybe.empty())
                  .flatMap(k -> Maybe.error(metadata().alreadyExisted(Strings.kvMsg(metadata().requestKeyName(), k))))
                  .switchIfEmpty(Single.just((P) AuditDecorator.addCreationAudit(reqData, metadata(), pojo)))
                  .flatMap(entity -> opt.isPresent()
                                     ? dao.insert((P) entity).map(i -> opt.get())
                                     : dao.insertReturningPrimary((P) entity));
    }

    @Override
    public Single<K> modifyReturningPrimary(RequestData reqData, EventAction action,
                                            BiFunction<VertxPojo, RequestData, VertxPojo> validator) {
        final K pk = metadata().parseKey(reqData);
        final D dao = entityHandler().dao(metadata().daoClass());
        //TODO validate unique keys
        return findOneByKey(reqData).map(
            db -> (P) AuditDecorator.addModifiedAudit(reqData, metadata(), db, validator.apply(db, reqData)))
                                    .flatMap(dao::update)
                                    .filter(i -> i > 0)
                                    .switchIfEmpty(Single.error(metadata().notFound(pk)))
                                    .map(i -> pk);
    }

    @Override
    public Single<P> deleteOneByKey(RequestData reqData) {
        final K pk = metadata().parseKey(reqData);
        return findOneByKey(reqData).flatMap(dbPojo -> isAbleToDelete(dbPojo, metadata(), this::pojoKeyMsg))
                                    .flatMap(pj -> entityHandler().dao(metadata().daoClass())
                                                                  .deleteById(pk)
                                                                  .filter(r -> r > 0)
                                                                  .map(r -> pj)
                                                                  .switchIfEmpty(EntityQueryExecutor.unableDelete(
                                                                      Strings.kvMsg(metadata().requestKeyName(), pk))));
    }

    @Override
    public Function<DSLContext, ResultQuery<R>> viewQuery(JsonObject filter, Pagination pagination) {
        return ctx -> (ResultQuery<R>) paging(
            ctx.select().from(metadata().table()).where(condition(metadata().table(), filter)), pagination);
    }

    @Override
    public Function<DSLContext, ResultQuery<R>> viewOneQuery(JsonObject filter) {
        return viewQuery(filter, Pagination.oneValue());
    }

    private String pojoKeyMsg(VertxPojo pojo) {
        final JsonObject json = pojo.toJson();
        final Object value = json.getValue(metadata().jsonKeyName());
        return Strings.kvMsg(metadata().requestKeyName(),
                             Optional.ofNullable(value).orElse(json.getValue(metadata().requestKeyName())));
    }

}
