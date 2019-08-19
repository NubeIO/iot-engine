package com.nubeiot.core.sql.query;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.ResultQuery;
import org.jooq.SelectJoinStep;

import io.github.jklingsporn.vertx.jooq.rx.RXQueryExecutor;
import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.jklingsporn.vertx.jooq.shared.internal.jdbc.JDBCQueryExecutor;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function3;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * For complex query with {@code join}, {@code group by}
 */
public interface ComplexQueryExecutor<P extends CompositePojo>
    extends ReferenceQueryExecutor<P>, JDBCQueryExecutor<Single<?>>, RXQueryExecutor {

    static ComplexQueryExecutor create(@NonNull EntityHandler handler) {
        return new DaoComplexQueryExecutor(handler);
    }

    ComplexQueryExecutor from(@NonNull CompositeMetadata metadata);

    ComplexQueryExecutor with(@NonNull EntityMetadata resourceMetadata);

    ComplexQueryExecutor context(@NonNull EntityMetadata contextMetadata);

    @SuppressWarnings("unchecked")
    class DaoComplexQueryExecutor<P extends CompositePojo> extends JDBCRXGenericQueryExecutor
        implements ComplexQueryExecutor<P> {

        private final EntityHandler handler;
        private final Map<String, EntityMetadata> references = new LinkedHashMap<>();
        private CompositeMetadata metadata;
        private EntityMetadata resourceRef;
        private EntityMetadata contextRef;

        DaoComplexQueryExecutor(EntityHandler handler) {
            super(handler.dsl().configuration(), io.vertx.reactivex.core.Vertx.newInstance(handler.vertx()));
            this.handler = handler;
        }

        @Override
        public ComplexQueryExecutor from(@NonNull CompositeMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        @Override
        public ComplexQueryExecutor with(@NonNull EntityMetadata resourceMetadata) {
            this.resourceRef = Optional.ofNullable(resourceRef).orElse(resourceMetadata);
            this.references.put(resourceMetadata.singularKeyName(), resourceMetadata);
            return this;
        }

        @Override
        public ComplexQueryExecutor context(@NonNull EntityMetadata contextMetadata) {
            this.contextRef = Optional.ofNullable(contextRef).orElse(contextMetadata);
            this.references.put(contextRef.singularKeyName(), contextRef);
            return this;
        }

        @Override
        public Observable<P> findMany(RequestData reqData) {
            final Pagination paging = Optional.ofNullable(reqData.getPagination()).orElse(Pagination.builder().build());
            return executeAny(viewQuery(reqData.getFilter(), paging)).map(r -> r.fetch(toMapper()))
                                                                     .flattenAsObservable(s -> s);
        }

        @Override
        public Single<P> findOneByKey(RequestData reqData) {
            Function<DSLContext, ResultQuery<? extends Record>> query = viewQuery(reqData.getFilter(),
                                                                                  Pagination.oneValue());
            return executeAny(query).map(r -> Optional.ofNullable(r.fetchOne(toMapper())))
                                    .map(o -> o.orElseThrow(() -> metadata.notFound(reqData, references.values())))
                                    .onErrorResumeNext(EntityQueryExecutor::wrapDatabaseError);
        }

        @Override
        public Maybe<P> lookupByPrimaryKey(@NonNull Object primaryKey) {
            return findOneById(metadata, primaryKey).map(o -> o.orElseThrow(() -> metadata.notFound(primaryKey)))
                                                    .map(p -> (P) metadata.convert(p))
                                                    .flatMapMaybe(Maybe::just);
        }

        @Override
        public Single<?> insertReturningPrimary(P pojo, RequestData reqData) {
            JsonObject refJson = JsonData.safeGet(reqData.body(), resourceRef.singularKeyName(), JsonObject.class);
            VertxPojo ref = Optional.ofNullable(refJson)
                                    .map((Function<JsonObject, VertxPojo>) resourceRef::parse)
                                    .orElse(null);
            if (Objects.isNull(ref)) {
                Object refKey = resourceRef.parseKey(reqData);
                reqData.getFilter()
                       .put(resourceRef.singularKeyName(), new JsonObject().put(resourceRef.jsonKeyName(), refKey));
                return checkExist(pojo, reqData, refKey).onErrorResumeNext(EntityQueryExecutor::wrapDatabaseError)
                                                        .flatMap(p -> (Single<?>) dao(metadata).insertReturningPrimary(
                                                            pojo));
            }
            final Object refKey = ref.toJson().getValue(resourceRef.jsonKeyName());
            return findOneById(resourceRef, refKey).flatMapMaybe(
                o -> o.isPresent() ? Maybe.error(resourceRef.alreadyExisted(refKey)) : Maybe.empty())
                                                   .switchIfEmpty(
                                                       (Single<?>) dao(resourceRef).insertReturningPrimary(ref))
                                                   .flatMap(rk -> (Single<?>) dao(metadata).insertReturningPrimary(
                                                       pojo.prop(resourceRef.requestKeyName(), rk)));
        }

        @Override
        public Single<?> modifyReturningPrimary(RequestData requestData, EventAction action,
                                                Function3<VertxPojo, VertxPojo, JsonObject, VertxPojo> validation) {
            throw new UnsupportedOperationException("Not yet supported");
        }

        @Override
        public Maybe<P> deleteOneByKey(RequestData requestData) {
            return findOneByKey(requestData).flatMapMaybe(pojo -> {
                final JsonTable<? extends Record> table = table(metadata);
                final Field field1 = table.field(
                    table.jsonFields().getOrDefault(metadata.jsonKeyName(), metadata.jsonKeyName()));
                final Field field2 = table.field(
                    table.jsonFields().getOrDefault(resourceRef.jsonKeyName(), resourceRef.jsonKeyName()));
                final Condition condition = field1.eq(metadata.parseKey(requestData))
                                                  .and(field2.eq(resourceRef.parseKey(requestData)));
                return ((Single<Integer>) handler.dao(metadata.daoClass()).deleteByCondition(condition)).filter(
                    r -> r > 0).map(r -> pojo).switchIfEmpty(Maybe.error(metadata.notFound(condition.toString())));
            });
        }

        private Single<P> checkExist(@NonNull P pojo, @NonNull RequestData reqData, @NonNull Object refKey) {
            Function<DSLContext, ResultQuery<? extends Record>> query = existQuery(reqData.getFilter());
            return executeAny(query).map(r -> Optional.ofNullable(r.fetchOne(toMapper())))
                                    .map(o -> o.orElseThrow(() -> resourceRef.notFound(refKey)))
                                    .filter(p -> Objects.isNull(p.prop(metadata.requestKeyName())))
                                    .switchIfEmpty(Single.error(new AlreadyExistException(Strings.format(
                                        "Resource with {0}={1} is already referenced to resource with {2}={3}",
                                        resourceRef.requestKeyName(), refKey, metadata.requestKeyName(),
                                        pojo.prop(metadata.jsonKeyName())))));
        }

        Function<DSLContext, ResultQuery<? extends Record>> viewQuery(@NonNull JsonObject filter,
                                                                      @NonNull Pagination pagination) {
            Predicate<Entry<String, EntityMetadata>> predicate = e -> Objects.nonNull(contextRef) &&
                                                                      !e.getKey().equals(contextRef.singularKeyName());
            return context -> join(context, ReferenceFilterCreation.createFilter(references, filter), JoinType.JOIN,
                                   predicate, false).limit(pagination.getPerPage())
                                                    .offset((pagination.getPage() - 1) * pagination.getPerPage());
        }

        Function<DSLContext, ResultQuery<? extends Record>> existQuery(JsonObject filter) {
            //            final List<Field> fields = Stream.concat(
            //                table(metadata).getKeys().stream().flatMap(k -> k.getFields().stream().map(Field
            //                .class::cast)),
            //                table(reference).getPrimaryKey().getFields().stream()).collect(Collectors.toList());
            return ctx -> join(ctx, ReferenceFilterCreation.createFilter(references, filter), JoinType.RIGHT_OUTER_JOIN,
                               x -> true, true).limit(1);
        }

        private RecordMapper<? super Record, P> toMapper() {
            return Objects.requireNonNull(metadata).mapper(resourceRef);
        }

        private VertxDAO dao(@NonNull EntityMetadata metadata) {
            return handler.dao(Objects.requireNonNull(metadata).daoClass());
        }

        private JsonTable<? extends Record> table(@NonNull EntityMetadata metadata) {
            return metadata.table();
        }

        private Single<Optional<? extends VertxPojo>> findOneById(@NonNull EntityMetadata metadata, Object refKey) {
            return Objects.isNull(refKey)
                   ? Single.just(Optional.empty())
                   : (Single<Optional<? extends VertxPojo>>) dao(metadata).findOneById(refKey);
        }

        private SelectJoinStep<Record> join(@NonNull DSLContext ctx, @NonNull JsonObject filter,
                                            @NonNull JoinType joinType,
                                            @NonNull Predicate<Entry<String, EntityMetadata>> predicate,
                                            boolean showKeyOnly) {
            SelectJoinStep<Record> base = ctx.select().from(filter(ctx, table(metadata), filter));
            references.entrySet().stream().filter(predicate).forEach(entry -> {
                final JsonTable<? extends Record> table = table(entry.getValue());
                final Field[] fields = showKeyOnly ? table.getPrimaryKey().getFieldsArray() : table.fields();
                base.join(filter(ctx, table, filter.getJsonObject(entry.getKey()), fields), joinType)
                    .using(table.getPrimaryKey().getFields());
            });
            return base;
        }

    }

}
