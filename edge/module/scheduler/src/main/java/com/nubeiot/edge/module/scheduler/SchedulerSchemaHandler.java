package com.nubeiot.edge.module.scheduler;

import org.jooq.Table;

import com.nubeiot.core.sql.SchemaHandler;
import com.nubeiot.core.sql.SchemaInitializer;
import com.nubeiot.core.sql.SchemaMigrator;
import com.nubeiot.iotdata.scheduler.model.Tables;

import lombok.NonNull;

class SchedulerSchemaHandler implements SchemaHandler {

    @Override
    public @NonNull Table table() {
        return Tables.JOB_TRIGGER;
    }

    @Override
    public @NonNull SchemaInitializer initializer() {
        return SchemaInitializer.NON_INITIALIZER;
    }

    @Override
    public @NonNull SchemaMigrator migrator() {
        return SchemaMigrator.NON_MIGRATOR;
    }

}
