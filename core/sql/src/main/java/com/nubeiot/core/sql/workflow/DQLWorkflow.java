package com.nubeiot.core.sql.workflow;

import com.nubeiot.core.sql.workflow.step.SQLStep.DQLStep;

import lombok.NonNull;

/**
 * DML workflow is used for querying data in a database
 *
 * @param <T> Type of {@code Result}
 * @see SQLWorkflow
 * @since 1.0.0
 */
public interface DQLWorkflow<T> extends SQLWorkflow {

    /**
     * Declares {@code DQL step}
     *
     * @return the DQL step
     * @since 1.0.0
     */
    @NonNull DQLStep<T> sqlStep();

}
