package com.nubeiot.core.sql.query;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.ResultQuery;

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

    ComplexQueryExecutor with(@NonNull EntityMetadata metadata);

    @SuppressWarnings("unchecked")
    class DaoComplexQueryExecutor<P extends CompositePojo> extends JDBCRXGenericQueryExecutor
        implements ComplexQueryExecutor<P> {

        private final EntityHandler handler;
        private CompositeMetadata metadata;
        private EntityMetadata reference;

        DaoComplexQueryExecutor(EntityHandler handler) {
            super(handler.dsl().configuration(), io.vertx.reactivex.core.Vertx.newInstance(handler.vertx()));
            this.handler = handler;
        }

        @Override
        public ComplexQueryExecutor from(CompositeMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        @Override
        public ComplexQueryExecutor with(EntityMetadata metadata) {
            this.reference = metadata;
            return this;
        }

        @Override
        public Observable<P> findMany(RequestData reqData) {
            Map<String, Object> refFilter = reqData.getFilter()
                                                   .fieldNames()
                                                   .stream()
                                                   .filter(s -> s.startsWith(reference.singularKeyName() + "."))
                                                   .collect(Collectors.toMap(
                                                       s -> s.replaceAll("^" + reference.singularKeyName() + "\\.", ""),
                                                       s -> reqData.getFilter().getValue(s)));
            reqData.getFilter().put(reference.singularKeyName(), refFilter);
            return executeAny(viewQuery(reqData)).map(r -> r.fetch(toMapper())).flattenAsObservable(s -> s);
        }

        @Override
        public Single<P> findOneByKey(RequestData reqData) {
            return executeAny(viewQuery(reqData)).map(r -> Optional.ofNullable(r.fetchOne(toMapper())))
                                                 .map(o -> o.orElseThrow(
                                                     () -> metadata.notFound(metadata.parsePrimaryKey(reqData))))
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
            final JsonObject refJson = JsonData.safeGet(reqData.body(), reference.singularKeyName(), JsonObject.class);
            VertxPojo ref = Optional.ofNullable(refJson).map(j -> reference.parse(j)).orElse(null);
            if (Objects.isNull(ref)) {
                Object refKey = reference.parsePrimaryKey(reqData);
                reqData.getFilter()
                       .put(reference.singularKeyName(), new JsonObject().put(reference.jsonKeyName(), refKey));
                return checkExist(pojo, reqData, refKey).onErrorResumeNext(EntityQueryExecutor::wrapDatabaseError)
                                                        .flatMap(p -> (Single<?>) dao(metadata).insertReturningPrimary(
                                                            pojo));
            }
            final Object refKey = ref.toJson().getValue(reference.jsonKeyName());
            return findOneById(reference, refKey).flatMapMaybe(
                o -> o.isPresent() ? Maybe.error(reference.alreadyExisted(refKey)) : Maybe.empty())
                                                 .switchIfEmpty((Single<?>) dao(reference).insertReturningPrimary(ref))
                                                 .flatMap(rk -> (Single<?>) dao(metadata).insertReturningPrimary(
                                                     pojo.prop(reference.requestKeyName(), rk)));
        }

        @Override
        public Single<?> modifyReturningPrimary(RequestData requestData, EventAction action,
                                                Function3<VertxPojo, VertxPojo, JsonObject, VertxPojo> validation) {
            throw new UnsupportedOperationException("Not yet supported");
        }

        @Override
        public Maybe<P> deleteByPrimary(P pojo, Object pk) {
            throw new UnsupportedOperationException("Not yet supported");
        }

        private Single<P> checkExist(@NonNull P pojo, @NonNull RequestData reqData, @NonNull Object refKey) {
            return executeAny(existQuery(reqData)).map(r -> Optional.ofNullable(r.fetchOne(toMapper())))
                                                  .map(o -> o.orElseThrow(() -> reference.notFound(refKey)))
                                                  .filter(p -> Objects.isNull(p.prop(metadata.requestKeyName())))
                                                  .switchIfEmpty(Single.error(new AlreadyExistException(Strings.format(
                                                      "Resource with {0}={1} is already referenced to resource with " +
                                                      "{2}={3}", reference.requestKeyName(), refKey,
                                                      metadata.requestKeyName(), pojo.prop(metadata.jsonKeyName())))));
        }

        Function<DSLContext, ResultQuery<? extends Record>> viewQuery(RequestData reqData) {
            final Pagination paging = reqData.getPagination();
            final JsonObject filter = reqData.getFilter();
            return ctx -> ctx.select()
                             .from(filter(ctx, table(metadata), filter))
                             .join(filter(ctx, table(reference), filter.getJsonObject(reference.singularKeyName())),
                                   JoinType.JOIN)
                             .using(table(reference).getPrimaryKey().getFields())
                             .limit(paging.getPerPage())
                             .offset((paging.getPage() - 1) * paging.getPerPage());
        }

        Function<DSLContext, ResultQuery<? extends Record>> existQuery(RequestData reqData) {
            final JsonObject filter = reqData.getFilter();
            //            final List<Field> fields = Stream.concat(
            //                table(metadata).getKeys().stream().flatMap(k -> k.getFields().stream().map(Field
            //                .class::cast)),
            //                table(reference).getPrimaryKey().getFields().stream()).collect(Collectors.toList());
            return ctx -> ctx.select()
                             .from(filter(ctx, table(metadata), filter))
                             .join(filter(ctx, table(reference), filter.getJsonObject(reference.singularKeyName())),
                                   JoinType.RIGHT_OUTER_JOIN)
                             .using(table(reference).getPrimaryKey().getFields())
                             .limit(1);
        }

        private RecordMapper<? super Record, P> toMapper() {
            return Objects.requireNonNull(metadata).mapper(reference);
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

    }

}
