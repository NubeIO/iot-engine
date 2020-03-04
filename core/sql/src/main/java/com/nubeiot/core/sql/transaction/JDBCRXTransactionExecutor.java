package com.nubeiot.core.sql.transaction;

import org.jooq.DSLContext;
import org.jooq.TransactionalCallable;

import io.reactivex.Single;

import lombok.NonNull;

public interface JDBCRXTransactionExecutor {

    static JDBCRXTransactionExecutor create(@NonNull DSLContext context) {
        return new JDBCRXTransactionExecutorImpl(context);
    }

    @NonNull DSLContext dsl();

    @NonNull <T> Single<T> transactionResult(TransactionalCallable<Single<T>> transactional);

}
