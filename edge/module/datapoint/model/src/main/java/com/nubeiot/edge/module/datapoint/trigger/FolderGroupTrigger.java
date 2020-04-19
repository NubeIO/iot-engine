package com.nubeiot.edge.module.datapoint.trigger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.h2.api.Trigger;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.SQLStateClass;
import org.jooq.impl.DSL;

import io.github.zero.utils.Strings;
import io.github.zero.utils.UUID64;

import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeDeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderGroupMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointTransducerMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.EdgeDeviceComposite;
import com.nubeiot.edge.module.datapoint.model.pojos.PointTransducerComposite;
import com.nubeiot.iotdata.dto.GroupLevel;
import com.nubeiot.iotdata.edge.model.tables.EdgeDevice;
import com.nubeiot.iotdata.edge.model.tables.PointTransducer;
import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

import lombok.NonNull;

public final class FolderGroupTrigger implements Trigger {

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type)
        throws SQLException { }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        try {
            final FolderGroup pojo = validateAndCompute(newRow, DSL.using(conn));
            newRow[1] = pojo.getLevel().type();
            newRow[2] = Strings.isBlank(pojo.getFolderId()) ? null : UUID64.uuid64ToUuid(pojo.getFolderId());
            newRow[3] = pojo.getNetworkId();
            newRow[4] = pojo.getDeviceId();
            newRow[5] = pojo.getPointId();
            newRow[6] = Strings.isBlank(pojo.getParentFolderId())
                        ? null
                        : UUID64.uuid64ToUuid(pojo.getParentFolderId());
        } catch (IllegalArgumentException cause) {
            throw new SQLException(SQLStateClass.C22_DATA_EXCEPTION.name(),
                                   SQLStateClass.C22_DATA_EXCEPTION.className(), cause);
        } catch (NubeException cause) {
            throw new SQLException(SQLStateClass.C23_INTEGRITY_CONSTRAINT_VIOLATION.name(),
                                   SQLStateClass.C23_INTEGRITY_CONSTRAINT_VIOLATION.className(), cause);
        }
    }

    @Override
    public void close() throws SQLException { }

    @Override
    public void remove() throws SQLException { }

    private FolderGroup validateAndCompute(@NonNull Object[] newRow, @NonNull DSLContext dsl) {
        final com.nubeiot.iotdata.edge.model.tables.FolderGroup table = FolderGroupMetadata.INSTANCE.table();
        final FolderGroup pojo = FolderGroupMetadata.INSTANCE.validate(
            new FolderGroup().setLevel(table.LEVEL.getDataType().convert(newRow[1]))
                             .setFolderId(table.FOLDER_ID.getDataType().convert(newRow[2]))
                             .setNetworkId(table.NETWORK_ID.getDataType().convert(newRow[3]))
                             .setDeviceId(table.DEVICE_ID.getDataType().convert(newRow[4]))
                             .setPointId(table.POINT_ID.getDataType().convert(newRow[5]))
                             .setParentFolderId(table.PARENT_FOLDER_ID.getDataType().convert(newRow[6])));
        if (pojo.getLevel() == GroupLevel.EDGE) {
            return computeOnEdgeLevel(dsl, pojo);
        }
        if (pojo.getLevel() == GroupLevel.NETWORK) {
            return computeOnNetworkLevel(dsl, pojo);
        }
        if (pojo.getLevel() == GroupLevel.DEVICE) {
            return computeOnDeviceLevel(dsl, pojo);
        }
        if (pojo.getLevel() == GroupLevel.FOLDER) {
            return computeByParentFolder(dsl, pojo);
        }
        throw new IllegalArgumentException("Unsupported group level " + pojo.getLevel());
    }

    private FolderGroup computeOnEdgeLevel(@NonNull DSLContext dsl, @NonNull FolderGroup pojo) {
        final com.nubeiot.iotdata.edge.model.tables.FolderGroup table = FolderGroupMetadata.INSTANCE.table();
        final Condition condition = table.LEVEL.eq(GroupLevel.EDGE)
                                               .and(table.FOLDER_ID.eq(pojo.getFolderId()))
                                               .and(table.NETWORK_ID.eq(pojo.getNetworkId()));
        if (dsl.fetchExists(table, condition)) {
            throw new AlreadyExistException(
                "Folder id " + pojo.getFolderId() + " is already assigned to network id " + pojo.getNetworkId());
        }
        return pojo;
    }

    private FolderGroup computeOnNetworkLevel(@NonNull DSLContext dsl, @NonNull FolderGroup group) {
        final com.nubeiot.iotdata.edge.model.tables.FolderGroup table = FolderGroupMetadata.INSTANCE.table();
        final UUID deviceId = group.getDeviceId();
        try {
            final Condition condition = table.LEVEL.eq(GroupLevel.NETWORK)
                                                   .and(table.FOLDER_ID.eq(group.getFolderId()))
                                                   .and(table.DEVICE_ID.eq(deviceId));
            if (dsl.fetchExists(table, condition)) {
                throw new AlreadyExistException(
                    "Folder id " + group.getFolderId() + " is already assigned to device id " + deviceId);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        final EdgeDevice edTable = EdgeDeviceMetadata.INSTANCE.table();
        final EdgeDeviceComposite ed = Optional.ofNullable(
            dsl.selectFrom(edTable).where(edTable.DEVICE_ID.eq(deviceId)).limit(1).fetchOne())
                                               .map(r -> r.into(EdgeDeviceMetadata.INSTANCE.modelClass()))
                                               .orElseThrow(
                                                   () -> new NotFoundException("Not found device id " + deviceId));
        if (Objects.nonNull(group.getNetworkId()) && !group.getNetworkId().equals(ed.getNetworkId())) {
            throw new IllegalArgumentException(
                "Device id " + deviceId + " does not belongs to network id " + ed.getNetworkId());
        }
        return group.setNetworkId(ed.getNetworkId());
    }

    private FolderGroup computeOnDeviceLevel(@NonNull DSLContext dsl, @NonNull FolderGroup group) {
        final com.nubeiot.iotdata.edge.model.tables.FolderGroup table = FolderGroupMetadata.INSTANCE.table();
        if (dsl.fetchExists(table, table.LEVEL.eq(GroupLevel.DEVICE)
                                              .and(table.FOLDER_ID.eq(group.getFolderId()))
                                              .and(table.POINT_ID.eq(group.getPointId())))) {
            throw new AlreadyExistException(
                "Folder id " + group.getFolderId() + " is already assigned to point id " + group.getPointId());
        }
        final Point pt = Optional.ofNullable(dsl.selectFrom(PointMetadata.INSTANCE.table())
                                                .where(PointMetadata.INSTANCE.table().ID.eq(group.getPointId()))
                                                .limit(1)
                                                .fetchOne())
                                 .map(r -> r.into(PointMetadata.INSTANCE.modelClass()))
                                 .orElseThrow(() -> new NotFoundException("Not found point id " + group.getPointId()));
        final UUID networkId = pt.getNetwork();
        if (Objects.nonNull(networkId) && !pt.getNetwork().equals(networkId)) {
            throw new IllegalArgumentException(
                "Point id " + group.getPointId() + " does not belongs to network id " + networkId);
        }
        final PointTransducer pointTransducerTable = PointTransducerMetadata.INSTANCE.table();
        final Condition condition = pointTransducerTable.POINT_ID.eq(group.getPointId())
                                                                 .and(pointTransducerTable.NETWORK_ID.eq(networkId));
        final PointTransducerComposite pTransducer = Optional.ofNullable(
            dsl.selectFrom(pointTransducerTable).where(condition).limit(1).fetchOne())
                                                             .map(r -> r.into(
                                                                 PointTransducerMetadata.INSTANCE.modelClass()))
                                                             .orElse(null);
        if (Objects.isNull(pTransducer)) {
            return group.setNetworkId(networkId);
        }
        if (Objects.nonNull(group.getDeviceId()) && !pTransducer.getDeviceId().equals(group.getDeviceId())) {
            throw new IllegalArgumentException(
                "Point id " + group.getPointId() + " does not belongs to device id " + group.getDeviceId());
        }
        return group.setNetworkId(networkId).setDeviceId(pTransducer.getDeviceId());
    }

    private FolderGroup computeByParentFolder(@NonNull DSLContext dsl, @NonNull FolderGroup pojo) {
        final com.nubeiot.iotdata.edge.model.tables.FolderGroup table = FolderGroupMetadata.INSTANCE.table();
        if (dsl.fetchExists(table, table.LEVEL.eq(GroupLevel.FOLDER)
                                              .and(table.FOLDER_ID.eq(pojo.getFolderId()))
                                              .and(table.PARENT_FOLDER_ID.eq(pojo.getParentFolderId())))) {
            throw new AlreadyExistException(
                "Folder id " + pojo.getFolderId() + " is already defined in folder id " + pojo.getNetworkId());
        }
        final FolderGroup parent = Optional.ofNullable(dsl.selectFrom(table)
                                                          .where(table.FOLDER_ID.eq(pojo.getParentFolderId())
                                                                                .and(table.LEVEL.eq(pojo.getLevel())))
                                                          .limit(1)
                                                          .fetchOne())
                                           .map(r -> r.into(FolderGroupMetadata.INSTANCE.modelClass()))
                                           .orElseThrow(() -> new NotFoundException(
                                               "Not found root folder id " + pojo.getParentFolderId()));
        return pojo.setNetworkId(parent.getNetworkId()).setDeviceId(parent.getDeviceId()).setPointId(null);
    }

}
