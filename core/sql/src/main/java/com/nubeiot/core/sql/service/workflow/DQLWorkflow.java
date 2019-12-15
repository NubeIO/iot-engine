package com.nubeiot.core.sql.service.workflow;

import com.nubeiot.core.sql.service.workflow.SQLStep.DQLStep;

import lombok.NonNull;

/**
 * DML workflow is used for querying data in a database
 */
public interface DQLWorkflow<T> extends SQLWorkflow {

    @NonNull DQLStep<T> query();

}
