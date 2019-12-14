package com.nubeiot.core.sql.query;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.ResultQuery;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.service.HasReferenceMarker;
import com.nubeiot.core.sql.service.HasReferenceMarker.EntityReferences;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

@SuppressWarnings("unchecked")
final class ComplexDaoQueryExecutor<CP extends CompositePojo> extends JDBCRXGenericQueryExecutor
    implements ComplexQueryExecutor<CP>, InternalQueryExecutor<CP> {

    private final EntityHandler handler;
    private CompositeMetadata base;
    private EntityMetadata context;
    private EntityMetadata resource;
    private Predicate<EntityMetadata> existPredicate = m -> Objects.nonNull(context) &&
                                                            !m.singularKeyName().equals(context.singularKeyName());
    private Predicate<EntityMetadata> viewPredicate = existPredicate;
    private EntityReferences references;

    ComplexDaoQueryExecutor(EntityHandler handler) {
        super(handler.dsl().configuration(), io.vertx.reactivex.core.Vertx.newInstance(handler.vertx()));
        this.handler = handler;
    }

    @Override
    public ComplexQueryExecutor from(@NonNull CompositeMetadata metadata) {
        this.base = metadata;
        this.context = Optional.ofNullable(context).orElse(metadata);
        return this;
    }

    @Override
    public ComplexQueryExecutor with(@NonNull EntityMetadata resourceMetadata) {
        this.resource = Optional.ofNullable(resource).orElse(resourceMetadata);
        return this;
    }

    @Override
    public ComplexQueryExecutor context(@NonNull EntityMetadata contextMetadata) {
        this.context = contextMetadata;
        return this;
    }

    @Override
    public ComplexQueryExecutor references(@NonNull EntityReferences references) {
        this.references = references;
        return this;
    }

    @Override
    public ComplexQueryExecutor viewPredicate(@NonNull Predicate<EntityMetadata> predicate) {
        this.viewPredicate = predicate;
        return this;
    }

    @Override
    public EntityHandler entityHandler() {
        return handler;
    }

    @Override
    public QueryBuilder queryBuilder() {
        return new QueryBuilder(base).references(Arrays.asList(resource, context)).predicate(viewPredicate);
    }

    @Override
    public Observable<CP> findMany(@NonNull RequestData requestData) {
        return executeAny(queryBuilder().view(requestData.filter(), requestData.sort(), requestData.pagination())).map(
            r -> r.fetch(toMapper())).flattenAsObservable(s -> s);
    }

    @Override
    public Single<CP> findOneByKey(@NonNull RequestData requestData) {
        final JsonObject filter = requestData.filter();
        final Single<? extends ResultQuery<? extends Record>> result = executeAny(
            queryBuilder().viewOne(filter, requestData.sort()));
        return result.map(r -> Optional.ofNullable(r.fetchOne(toMapper())))
                     .filter(Optional::isPresent)
                     .switchIfEmpty(Single.error(base.notFound(base.msg(filter, references.getFields().keySet()))))
                     .map(Optional::get)
                     .onErrorResumeNext(EntityQueryExecutor::sneakyThrowDBError);
    }

    @Override
    public Single<CP> lookupByPrimaryKey(@NonNull Object primaryKey) {
        final JsonObject filter = new JsonObject().put(base.jsonKeyName(), JsonData.checkAndConvert(primaryKey));
        return executeAny(queryBuilder().viewOne(filter, null)).map(r -> Optional.ofNullable(r.fetchOne(toMapper())))
                                                               .filter(Optional::isPresent)
                                                               .switchIfEmpty(Single.error(base.notFound(primaryKey)))
                                                               .map(Optional::get)
                                                               .flatMap(Single::just);
    }

    @Override
    public Single<?> insertReturningPrimary(@NonNull CP pojo, @NonNull RequestData reqData) {
        final VertxPojo src = pojo.safeGetOther(resource.singularKeyName(), resource.modelClass());
        final Object sKey = Optional.ofNullable(src)
                                    .map(r -> getKey(r.toJson(), resource))
                                    .orElse(resource.getKey(reqData)
                                                    .orElse(getKey(
                                                        JsonData.safeGet(reqData.body(), resource.singularKeyName(),
                                                                         JsonObject.class), resource)));
        final Object cKey = context.parseKey(reqData);
        if (Objects.isNull(src)) {
            if (Objects.isNull(sKey)) {
                throw new IllegalArgumentException("Missing " + resource.singularKeyName() + " data");
            }
            final JsonObject filter = reqData.filter();
            return isExist(cKey, sKey, filter).filter(p -> Objects.isNull(p.prop(context.requestKeyName())))
                                              .switchIfEmpty(Single.error(base.alreadyExisted(
                                                  base.msg(filter, references.getFields().keySet()))))
                                              .onErrorResumeNext(EntityQueryExecutor::sneakyThrowDBError)
                                              .map(k -> AuditDecorator.addCreationAudit(reqData, base, pojo))
                                              .flatMap(p -> (Single) dao(base).insertReturningPrimary(pojo));
        }
        final Maybe<Boolean> isExist = fetchExists(queryBuilder().exist(context, cKey));
        final String sKeyN = resource.requestKeyName();
        return isExist.flatMapSingle(b -> lookupByPrimaryKey(resource, sKey))
                      .filter(Optional::isPresent)
                      .flatMap(o -> Maybe.error(resource.alreadyExisted(Strings.kvMsg(sKeyN, sKey))))
                      .switchIfEmpty(Single.just(AuditDecorator.addCreationAudit(reqData, resource, src))
                                           .flatMap(p -> doInsertReturnKey(resource, p, sKey)))
                      .map(k -> AuditDecorator.addCreationAudit(reqData, base,
                                                                pojo.with(references.getFields().get(resource), k)))
                      .flatMap(p -> doInsertReturnKey(base, p, getKey(p.toJson(), base)));
    }

    @Override
    public Single<?> modifyReturningPrimary(@NonNull RequestData req, @NonNull EventAction action,
                                            @NonNull OperationValidator validator) {
        return findOneByKey(req).flatMap(db -> validator.validate(req, db))
                                .map(p -> (CP) p)
                                .flatMap(p -> Optional.ofNullable(p.getOther(resource.singularKeyName()))
                                                      .map(VertxPojo.class::cast)
                                                      .map(r -> (Single) dao(resource).update(
                                                          AuditDecorator.addModifiedAudit(req, resource, r)))
                                                      .orElse(Single.just(p))
                                                      .map(r -> p))
                                .flatMap(p -> dao(base).update(AuditDecorator.addModifiedAudit(req, base, (CP) p)));
    }

    @Override
    public Single<CP> deleteOneByKey(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        final Function<VertxPojo, String> function = pojo -> base.msg(pojo.toJson(), references.getFields().keySet());
        return findOneByKey(reqData).flatMap(dbEntity -> isAbleToDelete(dbEntity, base, function))
                                    .flatMap(dbEntity -> validator.validate(reqData, dbEntity))
                                    .map(dbEntity -> (CP) dbEntity)
                                    .flatMap(dbEntity -> doDelete(reqData, dbEntity));
    }

    @Override
    public @NonNull HasReferenceMarker marker() {
        throw new UnsupportedOperationException("Not using it in case of many-to-many");
    }

    @Override
    public EntityMetadata getMetadata() {
        return base;
    }

    private Object getKey(JsonObject data, @NonNull EntityMetadata metadata) {
        return Optional.ofNullable(data).map(d -> d.getValue(metadata.jsonKeyName())).orElse(null);
    }

    private Single<CP> isExist(@NonNull Object ctxKey, @NonNull Object resourceKey, @NonNull JsonObject filter) {
        final QueryBuilder queryBuilder = queryBuilder().predicate(existPredicate);
        final Maybe<Boolean> isExist = fetchExists(queryBuilder.exist(context, ctxKey));
        return isExist.switchIfEmpty(Single.error(context.notFound(ctxKey)))
                      .flatMap(e -> executeAny(queryBuilder.existQueryByJoin(filter)))
                      .map(r -> Optional.ofNullable(r.fetchOne(toMapper())))
                      .filter(Optional::isPresent)
                      .switchIfEmpty(Single.error(resource.notFound(resourceKey)))
                      .map(Optional::get);
    }

    private RecordMapper<? super Record, CP> toMapper() {
        return Objects.requireNonNull(base).mapper(resource, context);
    }

    private VertxDAO dao(@NonNull EntityMetadata metadata) {
        return handler.dao(metadata.daoClass());
    }

    private Single<? extends CP> doDelete(RequestData requestData, CP pojo) {
        final Object key = getKey(pojo.toJson(), base);
        if (Objects.isNull(key)) {
            return Single.error(new IllegalArgumentException("Missing " + base.jsonKeyName()));
        }
        Condition c = queryBuilder().conditionByPrimary(base, key);
        Single<Integer> result = (Single<Integer>) handler.dao(base.daoClass()).deleteByCondition(c);
        return result.filter(r -> r > 0)
                     .map(r -> pojo)
                     .switchIfEmpty(EntityQueryExecutor.unableDelete(
                         base.msg(requestData.filter(), references.getFields().keySet())));
    }

    private Single<?> doInsertReturnKey(@NonNull EntityMetadata metadata, @NonNull VertxPojo pojo, Object sKey) {
        return Objects.isNull(sKey)
               ? (Single<?>) dao(metadata).insertReturningPrimary(pojo)
               : ((Single<Integer>) dao(metadata).insert(pojo)).map(r -> sKey);
    }

}
