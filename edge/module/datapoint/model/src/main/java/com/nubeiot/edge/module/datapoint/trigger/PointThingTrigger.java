package com.nubeiot.edge.module.datapoint.trigger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

import org.h2.api.Trigger;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.exception.SQLStateClass;
import org.jooq.impl.DSL;

import com.nubeiot.core.exceptions.StateException;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointThingMetadata;
import com.nubeiot.iotdata.dto.ThingType;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointThing;
import com.nubeiot.iotdata.edge.model.tables.records.PointThingRecord;

public final class PointThingTrigger implements Trigger {

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type)
        throws SQLException { }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        final DSLContext context = DSL.using(conn);
        if (Functions.getIfThrow(() -> newRow[4]).isPresent()) {
            return;
        }
        final com.nubeiot.iotdata.edge.model.tables.PointThing table = Tables.POINT_THING;
        final PointThing pointThing = new PointThing().setPointId(table.POINT_ID.getDataType().convert(newRow[1]))
                                                      .setThingId(table.THING_ID.getDataType().convert(newRow[2]));
        final Record4<ThingType, UUID, UUID, UUID> record = context.select(Tables.THING.TYPE, Tables.THING.DEVICE_ID,
                                                                           Tables.EDGE_DEVICE.NETWORK_ID,
                                                                           Tables.EDGE_DEVICE.EDGE_ID)
                                                                   .from(Tables.THING.join(Tables.EDGE_DEVICE)
                                                                                     .on(Tables.THING.DEVICE_ID.eq(
                                                                                         Tables.EDGE_DEVICE.DEVICE_ID)))
                                                                   .where(Tables.THING.ID.eq(pointThing.getThingId()))
                                                                   .fetchOne();
        final ThingType type = record.get(Tables.THING.TYPE);
        final UUID deviceId = record.get(Tables.THING.DEVICE_ID);
        final UUID networkId = record.get(Tables.EDGE_DEVICE.NETWORK_ID);
        final UUID edgeId = record.get(Tables.EDGE_DEVICE.EDGE_ID);

        String computedThing = PointThingMetadata.genComputedThing(type, pointThing.getThingId());
        if (Strings.isNotBlank(computedThing)) {
            final PointThingRecord r = context.selectFrom(table)
                                              .where(table.COMPUTED_THING.eq(computedThing))
                                              .fetchOne();
            if (Objects.nonNull(r)) {
                throw new SQLException(SQLStateClass.C23_INTEGRITY_CONSTRAINT_VIOLATION.name(),
                                       SQLStateClass.C23_INTEGRITY_CONSTRAINT_VIOLATION.className(), new StateException(
                    Strings.format("Thing {0} with type {1} is already assigned to Point {2}", pointThing.getThingId(),
                                   type.type(), r.getPointId())));
            }
        }

        newRow[3] = computedThing;
        newRow[4] = deviceId;
        newRow[5] = networkId;
        newRow[6] = edgeId;
    }

    @Override
    public void close() throws SQLException { }

    @Override
    public void remove() throws SQLException { }

}
