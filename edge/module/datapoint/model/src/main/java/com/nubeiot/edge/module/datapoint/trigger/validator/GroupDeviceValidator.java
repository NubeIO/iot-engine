package com.nubeiot.edge.module.datapoint.trigger.validator;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;

import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.ConflictException;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointTransducerMetadata;
import com.nubeiot.edge.module.datapoint.trigger.validator.GroupLevelValidator.GroupLevelValidatorDecorator;
import com.nubeiot.iotdata.dto.GroupLevel;
import com.nubeiot.iotdata.edge.model.tables.PointTransducer;
import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;
import com.nubeiot.iotdata.edge.model.tables.records.PointRecord;
import com.nubeiot.iotdata.edge.model.tables.records.PointTransducerRecord;

import lombok.NonNull;

public final class GroupDeviceValidator extends AbstractGroupLevelValidator implements GroupLevelValidatorDecorator {

    public GroupDeviceValidator(@NonNull DSLContext dsl) {
        super(dsl);
    }

    @Override
    public @NonNull GroupLevelValidator initDecorator(@NonNull DSLContext dsl) {
        return new GroupNetworkValidator(dsl);
    }

    @Override
    public boolean shouldDecorator(@NonNull FolderGroup group) {
        return Objects.isNull(group.getPointId());
    }

    @NonNull
    public FolderRef computeReference(@NonNull FolderGroup group) {
        final PointRecord pt = Optional.ofNullable(dsl().selectFrom(PointMetadata.INSTANCE.table())
                                                        .where(PointMetadata.INSTANCE.table().ID.eq(group.getPointId()))
                                                        .limit(1)
                                                        .fetchOne())
                                       .orElseThrow(() -> PointMetadata.INSTANCE.notFound(group.getPointId()));
        final UUID networkId = pt.getNetwork();
        if (Objects.nonNull(networkId) && !pt.getNetwork().equals(networkId)) {
            throw new ConflictException(
                "Point id " + group.getPointId() + " does not belongs to network id " + networkId);
        }
        final PointTransducer table = PointTransducerMetadata.INSTANCE.table();
        final PointTransducerRecord record = Optional.ofNullable(dsl().selectFrom(table)
                                                                      .where(table.POINT_ID.eq(group.getPointId())
                                                                                           .and(table.NETWORK_ID.eq(
                                                                                               networkId)))
                                                                      .limit(1)
                                                                      .fetchOne()).orElse(null);
        if (Objects.isNull(record)) {
            return FolderRef.builder().networkId(networkId).build();
        }
        if (Objects.nonNull(group.getDeviceId()) && !record.getDeviceId().equals(group.getDeviceId())) {
            throw new ConflictException(
                "Point id " + group.getPointId() + " does not belongs to device id " + group.getDeviceId());
        }
        return FolderRef.builder().networkId(networkId).deviceId(record.getDeviceId()).pointId(pt.getId()).build();
    }

    @NonNull
    public FolderGroup validateExisted(@NonNull FolderGroup group) {
        if (dsl().fetchExists(table(), table().LEVEL.eq(GroupLevel.DEVICE)
                                                    .and(table().FOLDER_ID.eq(group.getFolderId()))
                                                    .and(table().POINT_ID.eq(group.getPointId())))) {
            throw new AlreadyExistException(
                "Folder id " + group.getFolderId() + " is already assigned to point id " + group.getPointId());
        }
        return group;
    }

}
