package com.nubeiot.core.sql.workflow;

public interface SQLTransactionWorkflow {

    /**
     * Declares {@code SQL workflow} in {@code database transaction}
     *
     * @return {@code true} if in transaction
     */
    boolean inTransaction();

}
