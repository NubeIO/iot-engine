package com.nubeiot.core.sql.query;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;

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
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings("unchecked")
abstract class BaseDaoQueryExecutor<P extends VertxPojo> implements InternalQueryExecutor<P> {

    private final EntityHandler handler;
    @Getter(value = AccessLevel.PUBLIC)
    private final EntityMetadata metadata;

    @Override
    public final EntityHandler entityHandler() {
        return handler;
    }

    @Override
    public QueryBuilder queryBuilder() {
        return new QueryBuilder(metadata);
    }

    @Override
    public Observable<P> findMany(RequestData reqData) {
        final Pagination paging = Optional.ofNullable(reqData.getPagination()).orElse(Pagination.builder().build());
        final Function<DSLContext, ? extends ResultQuery<? extends Record>> query = queryBuilder().view(
            reqData.getFilter(), reqData.getSort(), paging);
        return ((Single<List<P>>) entityHandler().dao(metadata.daoClass())
                                                 .queryExecutor()
                                                 .findMany(query)).flattenAsObservable(rs -> rs);
    }

    @Override
    public Single<?> insertReturningPrimary(@NonNull VertxPojo pojo, @NonNull RequestData reqData) {
        final @NonNull Optional<?> opt = Optional.ofNullable(pojo.toJson().getValue(metadata.jsonKeyName()))
                                                 .map(k -> metadata.parseKey(k.toString()));
        final VertxDAO dao = entityHandler().dao(metadata.daoClass());
        return opt.map(key -> fetchExists(queryBuilder().exist(metadata, key)).map(b -> key))
                  .orElse(Maybe.empty())
                  .flatMap(k -> Maybe.error(metadata.alreadyExisted(Strings.kvMsg(metadata.requestKeyName(), k))))
                  .switchIfEmpty(Single.just((P) AuditDecorator.addCreationAudit(reqData, metadata, pojo)))
                  .flatMap(entity -> opt.isPresent()
                                     ? ((Single<Integer>) dao.insert(entity)).map(i -> opt.get())
                                     : (Single<?>) dao.insertReturningPrimary(entity));
    }

    @Override
    public Single<?> modifyReturningPrimary(@NonNull RequestData reqData, @NonNull EventAction action,
                                            @NonNull OperationValidator validator) {
        final Object pk = metadata.parseKey(reqData);
        final VertxDAO dao = entityHandler().dao(metadata.daoClass());
        //TODO validate unique keys
        return findOneByKey(reqData).flatMap(db -> validator.validate(reqData, db).map(p -> new SimpleEntry<>(db, p)))
                                    .map(pojo -> AuditDecorator.addModifiedAudit(reqData, metadata, pojo.getKey(),
                                                                                 pojo.getValue()))
                                    .flatMap(p -> (Single<Integer>) dao.update(p))
                                    .filter(i -> i > 0)
                                    .switchIfEmpty(Single.error(metadata.notFound(pk)))
                                    .map(i -> pk);
    }

    @Override
    public Single<P> deleteOneByKey(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        final Object pk = metadata.parseKey(reqData);
        final VertxDAO dao = entityHandler().dao(metadata.daoClass());
        return findOneByKey(reqData).flatMap(dbPojo -> isAbleToDelete(dbPojo, metadata, this::pojoKeyMsg))
                                    .flatMap(dbPojo -> validator.validate(reqData, dbPojo))
                                    .flatMapMaybe(
                                        pj -> ((Single<Integer>) dao.deleteById(pk)).filter(r -> r > 0).map(r -> pj))
                                    .switchIfEmpty(
                                        EntityQueryExecutor.unableDelete(Strings.kvMsg(metadata.requestKeyName(), pk)));
    }

    @Override
    public final <X> Single<X> executeAny(Function<DSLContext, X> function) {
        return entityHandler().genericQuery().executeAny(function);
    }

    private String pojoKeyMsg(VertxPojo pojo) {
        final JsonObject json = pojo.toJson();
        final Object value = json.getValue(metadata.jsonKeyName());
        return Strings.kvMsg(metadata.requestKeyName(),
                             Optional.ofNullable(value).orElse(json.getValue(metadata.requestKeyName())));
    }

}
