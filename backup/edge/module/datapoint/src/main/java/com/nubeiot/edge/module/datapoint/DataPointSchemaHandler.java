package com.nubeiot.edge.module.datapoint;

import org.jooq.Table;

import com.nubeiot.core.sql.SchemaHandler;
import com.nubeiot.core.sql.SchemaInitializer;
import com.nubeiot.core.sql.SchemaMigrator;
import com.nubeiot.iotdata.edge.model.Tables;

import lombok.NonNull;

final class DataPointSchemaHandler implements SchemaHandler {

    @Override
    public @NonNull Table table() {
        return Tables.EDGE;
    }

    @Override
    public @NonNull SchemaInitializer initializer() {
        return new DataPointInitializer();
    }

    @Override
    public @NonNull SchemaMigrator migrator() {
        return new DataPointMigrator();
    }

}
