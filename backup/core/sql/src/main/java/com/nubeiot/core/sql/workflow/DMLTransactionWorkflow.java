package com.nubeiot.core.sql.workflow;

public interface DMLTransactionWorkflow extends DMLWorkflow, SQLTransactionWorkflow {

    @Override
    default boolean continueOnError() {
        return false;
    }

}
