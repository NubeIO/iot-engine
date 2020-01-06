package com.nubeiot.core.sql.workflow.step;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.query.EntityQueryExecutor;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
abstract class AbstractSQLStep implements SQLStep {

    @NonNull
    private final EventAction action;
    @NonNull
    private final EntityQueryExecutor queryExecutor;

}
