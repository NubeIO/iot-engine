package com.nubeiot.core.sql.workflow;

import com.nubeiot.core.sql.workflow.step.DMLBatchStep;

import lombok.NonNull;

/**
 * DML Batch workflow is used for adding (inserting), modifying (updating), and deleting data in batch into database.
 *
 * @see SQLWorkflow
 * @since 1.0.0
 */
public interface DMLBatchWorkflow extends SQLWorkflow {

    /**
     * Declares {@code DML Batch step}
     *
     * @return the DML step
     * @see DMLBatchStep
     * @since 1.0.0
     */
    @NonNull DMLBatchStep sqlStep();

}
