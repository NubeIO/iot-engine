package io.github.jklingsporn.vertx.jooq.rx.jdbc;

import java.util.function.Function;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.SQLStateClass;
import org.jooq.impl.DSL;

import io.github.jklingsporn.vertx.jooq.rx.RXQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.AbstractQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.github.jklingsporn.vertx.jooq.shared.internal.jdbc.JDBCQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.jdbc.JDBCQueryResult;
import io.github.zero88.utils.Functions;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.exceptions.DatabaseException;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;

import lombok.Getter;

/**
 * Created by jensklingsporn on 05.02.18.
 */
public class JDBCRXGenericQueryExecutor extends AbstractQueryExecutor
    implements JDBCQueryExecutor<Single<?>>, RXQueryExecutor {

    @Getter
    protected final Vertx vertx;

    public JDBCRXGenericQueryExecutor(Configuration configuration, Vertx vertx) {
        super(configuration);
        this.vertx = vertx;
    }

    @Override
    public <X> Single<X> executeAny(Function<DSLContext, X> function) {
        return executeBlocking(h -> h.complete(function.apply(DSL.using(configuration()))));
    }

    @SuppressWarnings("unchecked")
    <X> Single<X> executeBlocking(Handler<Future<X>> blockingCodeHandler) {
        return (Single<X>) vertx.rxExecuteBlocking(event -> {
            try {
                blockingCodeHandler.handle((Future<X>) event);
            } catch (DataAccessException e) {
                Throwable cause = Functions.getIfThrow(() -> e.getCause().getCause().getCause()).orElse(null);
                if (cause instanceof NubeException) {
                    if (e.sqlStateClass() == SQLStateClass.C22_DATA_EXCEPTION ||
                        e.sqlStateClass() == SQLStateClass.C23_INTEGRITY_CONSTRAINT_VIOLATION) {
                        throw (NubeException) cause;
                    }
                    throw new DatabaseException(
                        "Database error. Code: " + e.sqlStateClass() + " | Cause:" + cause.getMessage(),
                        new HiddenException(e.getCause()));
                }
                throw new DatabaseException("Database error. Code: " + e.sqlStateClass(), new HiddenException(e));
            }
        }).switchIfEmpty(Observable.empty().singleOrError());
    }

    @Override
    public Single<Integer> execute(Function<DSLContext, ? extends Query> queryFunction) {
        return executeBlocking(h -> h.complete(createQuery(queryFunction).execute()));
    }

    @Override
    public <R extends Record> Single<QueryResult> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return executeBlocking(h -> h.complete(new JDBCQueryResult(createQuery(queryFunction).fetch())));
    }

}
