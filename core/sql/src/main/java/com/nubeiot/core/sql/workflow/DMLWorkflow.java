package com.nubeiot.core.sql.workflow;

import com.nubeiot.core.sql.workflow.step.SQLStep.DMLStep;

import lombok.NonNull;

/**
 * DML workflow is used for adding (inserting), deleting, and modifying (updating) data in a database
 *
 * @see SQLWorkflow
 * @since 1.0.0
 */
public interface DMLWorkflow extends SQLWorkflow {

    /**
     * Declares {@code DML step}
     *
     * @return the DML step
     * @since 1.0.0
     */
    @NonNull DMLStep sqlStep();

}
