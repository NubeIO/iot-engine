package com.nubeiot.edge.module.datapoint.trigger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

import org.h2.api.Trigger;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.exception.SQLStateClass;
import org.jooq.impl.DSL;

import io.github.zero.utils.Functions;
import io.github.zero.utils.Strings;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
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
        if (Functions.getIfThrow(() -> newRow[6]).isPresent()) { // Tables.POINT_THING.EDGE_ID
            return;
        }
        final com.nubeiot.iotdata.edge.model.tables.PointThing table = Tables.POINT_THING;
        final PointThing pojo = new PointThing().setPointId(table.POINT_ID.getDataType().convert(newRow[1]))
                                                .setDeviceId(table.DEVICE_ID.getDataType().convert(newRow[2]))
                                                .setThingId(table.THING_ID.getDataType().convert(newRow[3]));
        final PointThing computedPointThing = Objects.isNull(pojo.getThingId())
                                              ? computePointThingWithoutThing(context, pojo)
                                              : computePointThingWithThing(context, pojo);
        newRow[2] = computedPointThing.getDeviceId();
        newRow[3] = computedPointThing.getThingId();
        newRow[4] = computedPointThing.getComputedThing();
        newRow[5] = computedPointThing.getNetworkId();
        newRow[6] = computedPointThing.getEdgeId();
    }

    @Override
    public void close() throws SQLException { }

    @Override
    public void remove() throws SQLException { }

    private PointThing computePointThingWithoutThing(DSLContext context, PointThing pojo) throws SQLException {
        if (Objects.isNull(pojo.getDeviceId())) {
            final NubeException cause = new NubeException(ErrorCode.INVALID_ARGUMENT, "Missing device id");
            throw new SQLException(SQLStateClass.C22_DATA_EXCEPTION.name(),
                                   SQLStateClass.C22_DATA_EXCEPTION.className(), cause);
        }
        final Record2<UUID, UUID> record = context.select(Tables.EDGE_DEVICE.NETWORK_ID, Tables.EDGE_DEVICE.EDGE_ID)
                                                  .from(Tables.EDGE_DEVICE)
                                                  .where(Tables.EDGE_DEVICE.DEVICE_ID.eq(pojo.getDeviceId()))
                                                  .fetchOne();
        return new PointThing(pojo).setNetworkId(record.get(Tables.EDGE_DEVICE.NETWORK_ID))
                                   .setEdgeId(record.get(Tables.EDGE_DEVICE.EDGE_ID));
    }

    private PointThing computePointThingWithThing(DSLContext context, PointThing pojo) throws SQLException {
        final Record4<ThingType, UUID, UUID, UUID> record = context.select(Tables.THING.TYPE, Tables.THING.DEVICE_ID,
                                                                           Tables.EDGE_DEVICE.NETWORK_ID,
                                                                           Tables.EDGE_DEVICE.EDGE_ID)
                                                                   .from(Tables.THING.join(Tables.EDGE_DEVICE)
                                                                                     .on(Tables.THING.DEVICE_ID.eq(
                                                                                         Tables.EDGE_DEVICE.DEVICE_ID)))
                                                                   .where(Tables.THING.ID.eq(pojo.getThingId()))
                                                                   .fetchOne();
        final UUID deviceId = record.get(Tables.THING.DEVICE_ID);
        if (Objects.nonNull(pojo.getDeviceId()) && !deviceId.equals(pojo.getDeviceId())) {
            final NubeException cause = new NubeException(ErrorCode.INVALID_ARGUMENT, Strings.format(
                "Input device id {0} is unmatched with referenced device id {1} in Thing {2}", pojo.getDeviceId(),
                deviceId, pojo.getThingId()));
            throw new SQLException(SQLStateClass.C22_DATA_EXCEPTION.name(),
                                   SQLStateClass.C22_DATA_EXCEPTION.className(), cause);
        }
        final ThingType type = record.get(Tables.THING.TYPE);
        final PointThing computed = new PointThing().setPointId(pojo.getPointId())
                                                    .setDeviceId(deviceId)
                                                    .setThingId(pojo.getThingId())
                                                    .setNetworkId(record.get(Tables.EDGE_DEVICE.NETWORK_ID))
                                                    .setEdgeId(record.get(Tables.EDGE_DEVICE.EDGE_ID))
                                                    .setComputedThing(
                                                        PointThingMetadata.genComputedThing(type, pojo.getThingId()));
        if (Strings.isNotBlank(computed.getComputedThing())) {
            final PointThingRecord r = context.selectFrom(Tables.POINT_THING)
                                              .where(Tables.POINT_THING.COMPUTED_THING.eq(computed.getComputedThing()))
                                              .fetchOne();
            if (Objects.nonNull(r)) {
                final NubeException cause = new NubeException(ErrorCode.INVALID_ARGUMENT, Strings.format(
                    "Thing {0} with type {1} is already assigned to Point {2}", pojo.getThingId(), type.type(),
                    r.getPointId()));
                throw new SQLException(SQLStateClass.C22_DATA_EXCEPTION.name(),
                                       SQLStateClass.C22_DATA_EXCEPTION.className(), cause);
            }
        }
        return computed;
    }

}
