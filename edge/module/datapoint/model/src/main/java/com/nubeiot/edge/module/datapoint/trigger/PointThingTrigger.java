package com.nubeiot.edge.module.datapoint.trigger;

import java.sql.Connection;
import java.sql.SQLException;

import org.h2.api.Trigger;

public class PointThingTrigger implements Trigger {

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type)
        throws SQLException {

    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {

    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public void remove() throws SQLException {

    }

}
