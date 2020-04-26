package com.nubeiot.edge.module.datapoint.trigger.validator;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;

import com.nubeiot.core.exceptions.ConflictException;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderGroupMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
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
        debug("Finding point is assigned to folder...");
        final PointCompositeMetadata pointMetadata = PointCompositeMetadata.INSTANCE;
        final PointRecord pointRecord = Optional.ofNullable(dsl().selectFrom(pointMetadata.table())
                                                                 .where(pointMetadata.table().ID.eq(group.getPointId()))
                                                                 .limit(1).fetchOne())
                                                .orElseThrow(() -> pointMetadata.notFound(group.getPointId()));
        final UUID networkId = pointRecord.getNetwork();
        debug("Comparing point network with folder network...");
        if (Objects.nonNull(networkId) && !pointRecord.getNetwork().equals(networkId)) {
            throw new ConflictException(
                "Point id " + group.getPointId() + " does not belongs to network id " + networkId);
        }
        // TODO: need to think strict relationship between device - point - folder
        final PointTransducer table = PointTransducerMetadata.INSTANCE.table();
        final Condition condition = table.POINT_ID.eq(group.getPointId()).and(table.NETWORK_ID.eq(networkId));
        final Optional<PointTransducerRecord> r = Optional.ofNullable(
            dsl().selectFrom(table).where(condition).limit(1).fetchOne());
        if (!r.isPresent()) {
            final FolderRef ref = initDecorator(dsl()).computeReference(group);
            return FolderRef.builder()
                            .networkId(ref.networkId())
                            .deviceId(ref.deviceId())
                            .pointId(pointRecord.getId())
                            .build();
        }
        final PointTransducerRecord rec = r.get();
        if (!rec.getDeviceId().equals(group.getDeviceId())) {
            throw new ConflictException(
                "Point id " + group.getPointId() + " does not belongs to device id " + group.getDeviceId());
        }
        return FolderRef.builder()
                        .networkId(rec.getNetworkId())
                        .deviceId(rec.getDeviceId())
                        .pointId(pointRecord.getId())
                        .build();
    }

    @NonNull
    public FolderGroup validateExisted(@NonNull FolderGroup group) {
        if (dsl().fetchExists(table(), table().LEVEL.eq(GroupLevel.DEVICE)
                                                    .and(table().FOLDER_ID.eq(group.getFolderId()))
                                                    .and(table().POINT_ID.eq(group.getPointId())))) {
            throw FolderGroupMetadata.INSTANCE.alreadyExisted(
                baseExistedLog(group) + " and point_id=" + group.getPointId());
        }
        return group;
    }

}
