package com.nubeiot.core.sql.query;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.AuditDecorator;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

@SuppressWarnings("unchecked")
class DaoComplexQueryExecutor<CP extends CompositePojo> extends JDBCRXGenericQueryExecutor
    implements ComplexQueryExecutor<CP> {

    private final EntityHandler handler;
    private final Map<String, EntityMetadata> references = new LinkedHashMap<>();
    private CompositeMetadata base;
    private EntityMetadata context;
    private EntityMetadata resource;

    DaoComplexQueryExecutor(EntityHandler handler) {
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
        this.references.put(resourceMetadata.singularKeyName(), resourceMetadata);
        return this;
    }

    @Override
    public ComplexQueryExecutor context(@NonNull EntityMetadata contextMetadata) {
        this.context = contextMetadata;
        this.references.put(this.context.singularKeyName(), this.context);
        return this;
    }

    @Override
    public EntityHandler entityHandler() {
        return handler;
    }

    @Override
    public Observable<CP> findMany(RequestData reqData) {
        return executeAny(viewQuery(reqData.getFilter(), reqData.getPagination())).map(r -> r.fetch(toMapper()))
                                                                                  .flattenAsObservable(s -> s);
    }

    @Override
    public Single<CP> findOneByKey(RequestData reqData) {
        final JsonObject filter = reqData.getFilter();
        return executeAny(viewOneQuery(filter)).map(r -> Optional.ofNullable(r.fetchOne(toMapper())))
                                               .filter(Optional::isPresent)
                                               .switchIfEmpty(
                                                   Single.error(base.notFound(base.msg(filter, references.values()))))
                                               .map(Optional::get)
                                               .onErrorResumeNext(EntityQueryExecutor::wrapDatabaseError);
    }

    @Override
    public Maybe<CP> lookupByPrimaryKey(@NonNull Object primaryKey) {
        return findOneById(base, primaryKey).filter(Optional::isPresent)
                                            .switchIfEmpty(Maybe.error(base.notFound(primaryKey)))
                                            .map(Optional::get)
                                            .map(p -> (CP) base.convert(p))
                                            .flatMap(Maybe::just);
    }

    @Override
    public Single<?> insertReturningPrimary(CP pojo, RequestData reqData) {
        final JsonObject src = JsonData.safeGet(reqData.body(), resource.singularKeyName(), JsonObject.class);
        final Object sKey = Optional.ofNullable(src)
                                    .map(r -> getKey(r, resource))
                                    .orElse(Functions.getIfThrow(() -> resource.parseKey(reqData)).orElse(null));
        final Object cKey = context.parseKey(reqData);
        final JsonObject query = reqData.getFilter()
                                        .copy()
                                        .put(context.singularKeyName(), putKey(cKey, context))
                                        .put(resource.singularKeyName(), putKey(sKey, resource));
        final String cKeyN = context.requestKeyName();
        final String sKeyN = resource.requestKeyName();
        if (Objects.isNull(src)) {
            return isExist(cKey, sKey, query).filter(p -> Objects.isNull(p.prop(cKeyN)))
                                             .switchIfEmpty(Single.error(base.alreadyExisted(
                                                 base.msg(reqData.getFilter(), references.values()))))
                                             .onErrorResumeNext(EntityQueryExecutor::wrapDatabaseError)
                                             .map(k -> AuditDecorator.addCreationAudit(reqData, base, pojo))
                                             .flatMap(p -> (Single) dao(base).insertReturningPrimary(pojo));
        }
        return isExist(context, cKey).flatMap(b -> findOneById(resource, sKey))
                                     .filter(Optional::isPresent)
                                     .flatMap(o -> Maybe.error(resource.alreadyExisted(Strings.kvMsg(sKeyN, sKey))))
                                     .switchIfEmpty((Single) dao(resource).insertReturningPrimary(
                                         AuditDecorator.addCreationAudit(reqData, resource,
                                                                         resource.parseFromRequest(src))))
                                     .map(k -> AuditDecorator.addCreationAudit(reqData, base, pojo.with(sKeyN, k)))
                                     .flatMap(p -> (Single) dao(base).insertReturningPrimary(p));
    }

    @Override
    public Single<?> modifyReturningPrimary(RequestData reqData, EventAction action,
                                            BiFunction<VertxPojo, RequestData, VertxPojo> validator) {
        return findOneByKey(reqData).map(db -> (CP) validator.apply(db, reqData))
                                    .flatMap(p -> Optional.ofNullable(p.getOther(resource.singularKeyName()))
                                                          .map(r -> (Single) dao(resource).update(
                                                              AuditDecorator.addModifiedAudit(reqData, resource,
                                                                                              (VertxPojo) r)))
                                                          .orElse(Single.just(p))
                                                          .map(r -> p))
                                    .flatMap(
                                        p -> dao(base).update(AuditDecorator.addModifiedAudit(reqData, base, (CP) p)));
    }

    @Override
    public Single<CP> deleteOneByKey(RequestData reqData) {
        return findOneByKey(reqData).flatMap(
            dbPojo -> isAbleToDelete(dbPojo, base, pojo -> base.msg(pojo.toJson(), references.values())))
                                    .flatMap(pojo -> doDelete(reqData, pojo));
    }

    public Function<DSLContext, ResultQuery<? extends Record>> viewQuery(@NonNull JsonObject filter,
                                                                         Pagination pagination) {
        Predicate<Entry<String, EntityMetadata>> predicate = e -> Objects.nonNull(context) &&
                                                                  !e.getKey().equals(context.singularKeyName());
        return context -> paging(
            join(context, ReferenceFilterCreation.createFilter(references, filter), JoinType.JOIN, predicate, false),
            pagination);
    }

    private Object getKey(JsonObject data, EntityMetadata metadata) {
        return data.getValue(metadata.jsonKeyName());
    }

    private Single<CP> isExist(@NonNull Object contextKey, @NonNull Object resourceKey,
                               @NonNull JsonObject baseFilter) {
        return isExist(context, contextKey).flatMap(
            e -> executeAny(existQueryByJoin(baseFilter)).map(r -> Optional.ofNullable(r.fetchOne(toMapper()))))
                                           .filter(Optional::isPresent)
                                           .switchIfEmpty(Single.error(resource.notFound(resourceKey)))
                                           .map(Optional::get);
    }

    private Single<Boolean> isExist(@NonNull EntityMetadata metadata, @NonNull Object ctxKey) {
        return executeAny(fetchExists(metadata, ctxKey)).filter(exist -> exist)
                                                        .switchIfEmpty(Single.error(metadata.notFound(ctxKey)));
    }

    private Function<DSLContext, ResultQuery<? extends Record>> existQueryByJoin(JsonObject filter) {
        Predicate<Entry<String, EntityMetadata>> predicate = e -> Objects.nonNull(context) &&
                                                                  !e.getKey().equals(context.singularKeyName());
        return dsl -> join(dsl, ReferenceFilterCreation.createFilter(references, filter), JoinType.RIGHT_OUTER_JOIN,
                           predicate, true).limit(1);
    }

    private RecordMapper<? super Record, CP> toMapper() {
        return Objects.requireNonNull(base).mapper(resource, context);
    }

    private VertxDAO dao(@NonNull EntityMetadata metadata) {
        return handler.dao(Objects.requireNonNull(metadata).daoClass());
    }

    private Single<Optional<? extends VertxPojo>> findOneById(@NonNull EntityMetadata metadata, Object key) {
        return Objects.isNull(key)
               ? Single.just(Optional.empty())
               : (Single<Optional<? extends VertxPojo>>) dao(metadata).findOneById(key);
    }

    private SelectConditionStep<Record> join(@NonNull DSLContext ctx, @NonNull JsonObject filter,
                                             @NonNull JoinType joinType,
                                             @NonNull Predicate<Entry<String, EntityMetadata>> predicate,
                                             boolean checkExist) {
        final JsonTable<? extends Record> from = base.table();
        final SelectJoinStep<Record> baseQuery = ctx.select(joinFields(from, checkExist, predicate)).from(from);
        final JsonObject baseFilter = checkExist ? new JsonObject() : filter.copy();
        references.entrySet().forEach(entry -> {
            if (predicate.test(entry)) {
                doJoin(baseQuery, from, entry.getValue(), joinType, filter);
            } else {
                baseFilter.put(entry.getValue().requestKeyName(), filter.getValue(entry.getValue().requestKeyName()));
            }
        });
        return baseQuery.where(condition(from, baseFilter, checkExist));
    }

    private void doJoin(@NonNull SelectJoinStep<Record> base, @NonNull JsonTable<? extends Record> from,
                        @NonNull EntityMetadata refMetadata, @NonNull JoinType joinType, @NonNull JsonObject filter) {
        JsonObject f = filter.getJsonObject(refMetadata.singularKeyName());
        base.join(refMetadata.table(), joinType).onKey().where(condition(refMetadata.table(), f));
    }

    private Condition joinCondition(JsonTable<? extends Record> from, JsonTable<? extends Record> ref) {
        final ForeignKey<? extends Record, ? extends Record> fk = from.getReferencesTo(ref)
                                                                      .stream()
                                                                      .findFirst()
                                                                      .orElseThrow(noReference(from, ref));
        final Field refField = ref.getPrimaryKey().getFields().get(0);
        return from.field(fk.getFields().get(0)).eq(refField);
    }

    private List<SelectFieldOrAsterisk> joinFields(@NonNull JsonTable<? extends Record> from, boolean showKeyOnly,
                                                   @NonNull Predicate<Entry<String, EntityMetadata>> predicate) {
        if (!showKeyOnly) {
            return Collections.singletonList(DSL.asterisk());
        }
        return Stream.concat(Stream.of(from.asterisk()), references.entrySet()
                                                                   .stream()
                                                                   .filter(predicate)
                                                                   .map(entry -> entry.getValue()
                                                                                      .table()
                                                                                      .getPrimaryKey()
                                                                                      .getFieldsArray())
                                                                   .flatMap(Stream::of)).collect(Collectors.toList());
    }

    private Supplier<ImplementationError> noReference(JsonTable<? extends Record> from,
                                                      JsonTable<? extends Record> ref) {
        return () -> new ImplementationError(ErrorCode.DATABASE_ERROR,
                                             "No foreign key between " + from.getName() + " and " + ref.getName());
    }

    private JsonObject putKey(Object cKey, EntityMetadata context) {
        return new JsonObject().put(context.jsonKeyName(), cKey);
    }

    private SingleSource<? extends CP> doDelete(RequestData requestData, CP pojo) {
        Condition c = conditionByPrimary(base, getKey(pojo.toJson(), base));
        Single<Integer> result = (Single<Integer>) handler.dao(base.daoClass()).deleteByCondition(c);
        return result.filter(r -> r > 0)
                     .map(r -> pojo)
                     .switchIfEmpty(
                         EntityQueryExecutor.unableDelete(base.msg(requestData.getFilter(), references.values())));
    }

}
