package com.nubeiot.core.sql.query;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jooq.Configuration;
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
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings("unchecked")
abstract class BaseDaoQueryExecutor<P extends VertxPojo> implements InternalQueryExecutor<P> {

    @NonNull
    private final EntityHandler entityHandler;
    @NonNull
    private final EntityMetadata metadata;
    @Setter
    private Configuration runtimeConfiguration;

    public final Configuration runtimeConfiguration() {
        return Optional.ofNullable(runtimeConfiguration).orElseGet(entityHandler.dsl()::configuration);
    }

    @Override
    public QueryBuilder queryBuilder() {
        return new QueryBuilder(metadata);
    }

    @Override
    public Observable<P> findMany(@NonNull RequestData reqData) {
        final Pagination paging = Optional.ofNullable(reqData.pagination()).orElse(Pagination.builder().build());
        final Function<DSLContext, ? extends ResultQuery<? extends Record>> query = queryBuilder().view(reqData.filter(), reqData.sort(), paging);
        final VertxDAO dao = dao(metadata.daoClass());
        return ((Single<List<P>>) dao.queryExecutor().findMany(query)).flattenAsObservable(rs -> rs);
    }

    @Override
    public @NonNull Single<DMLPojo> insertReturningPrimary(@NonNull RequestData reqData,
                                                           @NonNull OperationValidator validator) {
        final VertxDAO dao = dao(metadata.daoClass());
        return validator.validate(reqData, null).flatMap(pojo -> {
            final Optional<Object> opt = Optional.ofNullable(pojo.toJson().getValue(metadata.jsonKeyName()))
                                                 .map(k -> metadata.parseKey(k.toString()));
            return opt.map(key -> fetchExists(queryBuilder().exist(metadata, key)).map(b -> key))
                      .orElse(Maybe.empty())
                      .flatMap(k -> Maybe.error(metadata.alreadyExisted(Strings.kvMsg(metadata.requestKeyName(), k))))
                      .switchIfEmpty(Single.just((P) AuditDecorator.addCreationAudit(reqData, metadata, pojo)))
                      .flatMap(entity -> opt.isPresent()
                                         ? ((Single<Integer>) dao.insert(entity)).map(i -> opt.get())
                                         : (Single<?>) dao.insertReturningPrimary(entity))
                      .map(key -> DMLPojo.builder().request(pojo).primaryKey(key).build());
        });
    }

    @Override
    public @NonNull Single<DMLPojo> modifyReturningPrimary(@NonNull RequestData reqData,
                                                           @NonNull OperationValidator validator) {
        final Object pk = metadata.parseKey(reqData);
        final VertxDAO dao = dao(metadata.daoClass());
        //TODO validate unique keys
        return findOneByKey(reqData).flatMap(db -> validator.validate(reqData, db).map(p -> new SimpleEntry<>(db, p)))
                                    .map(entry -> AuditDecorator.addModifiedAudit(reqData, metadata, entry.getKey(),
                                                                                  entry.getValue()))
                                    .flatMapMaybe(p -> ((Single<Integer>) dao.update(p)).filter(i -> i > 0).map(r -> p))
                                    .switchIfEmpty(Single.error(metadata.notFound(pk)))
                                    .map(p -> DMLPojo.builder().request(p).primaryKey(pk).build());
    }

    @Override
    public Single<P> deleteOneByKey(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        final Object pk = metadata.parseKey(reqData);
        final VertxDAO dao = dao(metadata.daoClass());
        return findOneByKey(reqData).flatMap(dbPojo -> isAbleToDelete(dbPojo, metadata, this::pojoKeyMsg))
                                    .flatMap(dbPojo -> validator.validate(reqData, dbPojo))
                                    .flatMapMaybe(
                                        p -> ((Single<Integer>) dao.deleteById(pk)).filter(r -> r > 0).map(r -> p))
                                    .switchIfEmpty(
                                        EntityQueryExecutor.unableDelete(Strings.kvMsg(metadata.requestKeyName(), pk)));
    }

    @Override
    public final <X> Single<X> executeAny(Function<DSLContext, X> function) {
        return entityHandler().genericQuery(runtimeConfiguration()).executeAny(function);
    }

    private String pojoKeyMsg(VertxPojo pojo) {
        final JsonObject json = pojo.toJson();
        final Object value = json.getValue(metadata.jsonKeyName());
        return Strings.kvMsg(metadata.requestKeyName(),
                             Optional.ofNullable(value).orElse(json.getValue(metadata.requestKeyName())));
    }

}
