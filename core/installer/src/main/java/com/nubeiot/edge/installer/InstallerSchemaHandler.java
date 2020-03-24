package com.nubeiot.edge.installer;

import org.jooq.Table;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.SchemaHandler;
import com.nubeiot.core.sql.SchemaInitializer;
import com.nubeiot.core.sql.SchemaMigrator;
import com.nubeiot.edge.installer.model.Tables;

import lombok.NonNull;

final class InstallerSchemaHandler implements SchemaHandler {

    @Override
    public @NonNull Table table() {
        return Tables.APPLICATION;
    }

    @Override
    public @NonNull SchemaInitializer initializer() {
        return entityHandler -> {
            InstallerEntityHandler handler = (InstallerEntityHandler) entityHandler;
            final InstallerConfig config = handler.sharedData(InstallerEntityHandler.SHARED_INSTALLER_CFG);
            return handler.addBuiltinApps(config);
        };
    }

    @Override
    public @NonNull SchemaMigrator migrator() {
        return entityHandler -> {
            InstallerEntityHandler handler = (InstallerEntityHandler) entityHandler;
            return handler.transitionPendingModules().map(r -> EventMessage.success(EventAction.MIGRATE, r));
        };
    }

}
