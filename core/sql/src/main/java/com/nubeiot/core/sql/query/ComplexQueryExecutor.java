package com.nubeiot.core.sql.query;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.ResultQuery;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function3;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.tables.JsonTable;

import lombok.NonNull;

/**
 * For complex query with {@code join}, {@code group by}
 */
public interface ComplexQueryExecutor extends ReferenceQueryExecutor {

    static ComplexQueryExecutor create(EntityHandler handler) {
        return new DefaultComplexQueryExecutor(handler);
    }

    <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> ComplexQueryExecutor from(
        Class<D> daoClass);

    ComplexQueryExecutor from(@NonNull JsonTable<? extends Record> table);

    default ComplexQueryExecutor with(@NonNull JsonTable<? extends Record> table) {
        return with(table, JoinType.JOIN);
    }

    ComplexQueryExecutor with(@NonNull JsonTable<? extends Record> table, @NonNull JoinType joinType);

    <R extends Record, E extends VertxPojo> ComplexQueryExecutor mapper(@NonNull RecordMapper<R, E> mapper);

    @SuppressWarnings("unchecked")
    class DefaultComplexQueryExecutor extends JDBCRXGenericQueryExecutor implements ComplexQueryExecutor {

        private final EntityHandler handler;
        private JsonTable<? extends Record> from;
        private JsonTable<? extends Record> with;
        private JoinType joinType;
        private RecordMapper<Record, VertxPojo> mapper;
        private Class<? extends VertxDAO> dao;

        DefaultComplexQueryExecutor(EntityHandler handler) {
            super(handler.dsl().configuration(), handler.getVertx());
            this.handler = handler;
        }

        @Override
        public <K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> ComplexQueryExecutor from(
            Class<D> daoClass) {
            this.dao = daoClass;
            return this;
        }

        @Override
        public ComplexQueryExecutor from(@NonNull JsonTable<? extends Record> table) {
            this.from = table;
            return this;
        }

        @Override
        public ComplexQueryExecutor with(@NonNull JsonTable<? extends Record> table, @NonNull JoinType joinType) {
            this.with = table;
            this.joinType = joinType;
            return this;
        }

        public <R extends Record, P extends VertxPojo> ComplexQueryExecutor mapper(@NonNull RecordMapper<R, P> mapper) {
            this.mapper = (RecordMapper<Record, VertxPojo>) mapper;
            return this;
        }

        @Override
        public Observable<VertxPojo> findMany(RequestData reqData) {
            return executeAny(query(reqData)).map(r -> r.fetch(mapper)).flattenAsObservable(s -> s);
        }

        @Override
        public Maybe<? extends VertxPojo> findOne(RequestData reqData) {
            return executeAny(query(reqData)).map(r -> Optional.ofNullable(r.fetchOne(mapper)))
                                             .flatMapMaybe(o -> o.map(Maybe::just).orElse(Maybe.empty()));
        }

        @Override
        public Maybe<? extends VertxPojo> lookupById(@NonNull Object primaryKey) {
            return null;
        }

        @Override
        public Single<?> insertReturningPrimary(VertxPojo pojo) {
            return (Single<?>) handler.dao(Objects.requireNonNull(dao)).insertReturningPrimary(pojo);
        }

        @Override
        public Single<Object> modifyReturningPrimary(RequestData requestData, EventAction action,
                                                     Function3<VertxPojo, VertxPojo, JsonObject, VertxPojo> validation) {
            return null;
        }

        Function<DSLContext, ResultQuery<? extends Record>> query(RequestData reqData) {
            final Pagination paging = reqData.getPagination();
            final JsonObject filter = reqData.getFilter();
            //TODO should filter by reference also
            return ctx -> ctx.select()
                             .from(filter(ctx, Objects.requireNonNull(from), filter))
                             .join(Objects.requireNonNull(with), joinType)
                             .using(with.getPrimaryKey().getFields())
                             .limit(paging.getPerPage())
                             .offset((paging.getPage() - 1) * paging.getPerPage());
        }

    }

}
