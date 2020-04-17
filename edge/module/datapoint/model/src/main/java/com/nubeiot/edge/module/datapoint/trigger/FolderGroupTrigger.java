package com.nubeiot.edge.module.datapoint.trigger;

import java.sql.Connection;
import java.sql.SQLException;

import org.h2.api.Trigger;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import io.github.zero.utils.Functions;

import com.nubeiot.iotdata.edge.model.Tables;

public final class FolderGroupTrigger implements Trigger {

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type)
        throws SQLException { }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        final DSLContext context = DSL.using(conn);
        if (Functions.getIfThrow(() -> newRow[6]).isPresent()) {
            return;
        }
        final com.nubeiot.iotdata.edge.model.tables.FolderGroup table = Tables.FOLDER_GROUP;
    }

    @Override
    public void close() throws SQLException { }

    @Override
    public void remove() throws SQLException { }

}
