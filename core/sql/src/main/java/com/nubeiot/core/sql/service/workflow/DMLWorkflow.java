package com.nubeiot.core.sql.service.workflow;

import com.nubeiot.core.sql.service.workflow.SQLStep.DMLStep;

import lombok.NonNull;

/**
 * DML workflow is used for adding (inserting), deleting, and modifying (updating) data in a database
 */
public interface DMLWorkflow extends SQLWorkflow {

    @NonNull DMLStep persist();

}
