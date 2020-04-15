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
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointTransducerMetadata;
import com.nubeiot.iotdata.dto.TransducerType;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointTransducer;
import com.nubeiot.iotdata.edge.model.tables.records.PointTransducerRecord;

public final class PointTransducerTrigger implements Trigger {

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type)
        throws SQLException { }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        final DSLContext context = DSL.using(conn);
        if (Functions.getIfThrow(() -> newRow[6]).isPresent()) { // Tables.POINT_TRANSDUCER.EDGE_ID
            return;
        }
        final com.nubeiot.iotdata.edge.model.tables.PointTransducer table = Tables.POINT_TRANSDUCER;
        final PointTransducer pojo = new PointTransducer().setPointId(table.POINT_ID.getDataType().convert(newRow[1]))
                                                          .setDeviceId(table.DEVICE_ID.getDataType().convert(newRow[2]))
                                                          .setTransducerId(
                                                              table.TRANSDUCER_ID.getDataType().convert(newRow[3]));
        final PointTransducer computedPointTransducer = Objects.isNull(pojo.getTransducerId())
                                                        ? computeWithoutTransducer(context, pojo)
                                                        : computeWithTransducer(context, pojo);
        newRow[2] = computedPointTransducer.getDeviceId();
        newRow[3] = computedPointTransducer.getTransducerId();
        newRow[4] = computedPointTransducer.getComputedTransducer();
        newRow[5] = computedPointTransducer.getNetworkId();
        newRow[6] = computedPointTransducer.getEdgeId();
    }

    @Override
    public void close() throws SQLException { }

    @Override
    public void remove() throws SQLException { }

    private PointTransducer computeWithoutTransducer(DSLContext context, PointTransducer pojo) throws SQLException {
        if (Objects.isNull(pojo.getDeviceId())) {
            final NubeException cause = new NubeException(ErrorCode.INVALID_ARGUMENT, "Missing device id");
            throw new SQLException(SQLStateClass.C22_DATA_EXCEPTION.name(),
                                   SQLStateClass.C22_DATA_EXCEPTION.className(), cause);
        }
        final Record2<UUID, UUID> record = context.select(Tables.EDGE_DEVICE.NETWORK_ID, Tables.EDGE_DEVICE.EDGE_ID)
                                                  .from(Tables.EDGE_DEVICE)
                                                  .where(Tables.EDGE_DEVICE.DEVICE_ID.eq(pojo.getDeviceId()))
                                                  .fetchOne();
        return new PointTransducer(pojo).setNetworkId(record.get(Tables.EDGE_DEVICE.NETWORK_ID))
                                        .setEdgeId(record.get(Tables.EDGE_DEVICE.EDGE_ID));
    }

    private PointTransducer computeWithTransducer(DSLContext context, PointTransducer pojo) throws SQLException {
        final Record4<TransducerType, UUID, UUID, UUID> record = context.select(Tables.TRANSDUCER.TYPE,
                                                                                Tables.TRANSDUCER.DEVICE_ID,
                                                                                Tables.EDGE_DEVICE.NETWORK_ID,
                                                                                Tables.EDGE_DEVICE.EDGE_ID)
                                                                        .from(Tables.TRANSDUCER.join(Tables.EDGE_DEVICE)
                                                                                               .on(Tables.TRANSDUCER.DEVICE_ID
                                                                                                       .eq(Tables.EDGE_DEVICE.DEVICE_ID)))
                                                                        .where(Tables.TRANSDUCER.ID.eq(
                                                                            pojo.getTransducerId()))
                                                                        .fetchOne();
        final UUID deviceId = record.get(Tables.TRANSDUCER.DEVICE_ID);
        if (Objects.nonNull(pojo.getDeviceId()) && !deviceId.equals(pojo.getDeviceId())) {
            final NubeException cause = new NubeException(ErrorCode.INVALID_ARGUMENT, Strings.format(
                "Input device id {0} is unmatched with referenced device id {1} in transducer {2}", pojo.getDeviceId(),
                deviceId, pojo.getTransducerId()));
            throw new SQLException(SQLStateClass.C22_DATA_EXCEPTION.name(),
                                   SQLStateClass.C22_DATA_EXCEPTION.className(), cause);
        }
        final TransducerType type = record.get(Tables.TRANSDUCER.TYPE);
        final PointTransducer computed = new PointTransducer().setPointId(pojo.getPointId())
                                                              .setDeviceId(deviceId)
                                                              .setTransducerId(pojo.getTransducerId())
                                                              .setNetworkId(record.get(Tables.EDGE_DEVICE.NETWORK_ID))
                                                              .setEdgeId(record.get(Tables.EDGE_DEVICE.EDGE_ID))
                                                              .setComputedTransducer(
                                                                  PointTransducerMetadata.genComputedTransducer(type,
                                                                                                                pojo.getTransducerId()));
        if (Strings.isNotBlank(computed.getComputedTransducer())) {
            final PointTransducerRecord r = context.selectFrom(Tables.POINT_TRANSDUCER)
                                                   .where(Tables.POINT_TRANSDUCER.COMPUTED_TRANSDUCER.eq(
                                                       computed.getComputedTransducer()))
                                                   .fetchOne();
            if (Objects.nonNull(r)) {
                final NubeException cause = new NubeException(ErrorCode.INVALID_ARGUMENT, Strings.format(
                    "Transducer {0} with type {1} is already assigned to Point {2}", pojo.getTransducerId(),
                    type.type(), r.getPointId()));
                throw new SQLException(SQLStateClass.C22_DATA_EXCEPTION.name(),
                                       SQLStateClass.C22_DATA_EXCEPTION.className(), cause);
            }
        }
        return computed;
    }

}
