package com.nubeiot.core.sql.workflow.task;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.cache.EntityServiceIndex;
import com.nubeiot.core.sql.query.EntityQueryExecutor;

import lombok.NonNull;

public interface PurgeDefinitionContext extends EntityDefinitionContext {

    static PurgeDefinitionContext create(@NonNull EntityQueryExecutor queryExecutor) {
        return () -> queryExecutor;
    }

    @NonNull EntityQueryExecutor queryExecutor();

    @Override
    default @NonNull EntityHandler entityHandler() {
        return queryExecutor().entityHandler();
    }

    @NonNull
    default EntityServiceIndex entityServiceIndex() {
        return entityHandler().sharedData(EntityServiceIndex.DATA_KEY);
    }

}
