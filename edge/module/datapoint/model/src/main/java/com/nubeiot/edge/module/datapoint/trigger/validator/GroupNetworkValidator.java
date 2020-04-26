package com.nubeiot.edge.module.datapoint.trigger.validator;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;

import com.nubeiot.core.exceptions.ConflictException;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeDeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderGroupMetadata;
import com.nubeiot.edge.module.datapoint.trigger.validator.GroupLevelValidator.GroupLevelValidatorDecorator;
import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;
import com.nubeiot.iotdata.edge.model.tables.records.EdgeDeviceRecord;

import lombok.NonNull;

public final class GroupNetworkValidator extends AbstractGroupLevelValidator implements GroupLevelValidatorDecorator {

    public GroupNetworkValidator(@NonNull DSLContext dsl) {
        super(dsl);
    }

    @Override
    public @NonNull GroupLevelValidator initDecorator(@NonNull DSLContext dsl) {
        return new GroupEdgeValidator(dsl);
    }

    @Override
    public boolean shouldDecorator(@NonNull FolderGroup group) {
        return Objects.isNull(group.getDeviceId());
    }

    public FolderRef computeReference(@NonNull FolderGroup group) {
        debug("Finding device is assigned to folder...");
        final UUID id = group.getDeviceId();
        final EdgeDeviceRecord r = Optional.ofNullable(dsl().selectFrom(EdgeDeviceMetadata.INSTANCE.table())
                                                            .where(EdgeDeviceMetadata.INSTANCE.table().DEVICE_ID.eq(id))
                                                            .limit(1)
                                                            .fetchOne())
                                           .orElseThrow(() -> DeviceMetadata.INSTANCE.notFound(id));
        debug("Comparing device network with folder network...");
        if (Objects.nonNull(group.getNetworkId()) && !group.getNetworkId().equals(r.getNetworkId())) {
            throw new ConflictException("Device id " + id + " does not belongs to network id " + r.getNetworkId());
        }
        return FolderRef.builder().networkId(r.getNetworkId()).deviceId(id).build();
    }

    public FolderGroup validateExisted(@NonNull FolderGroup group) {
        final UUID deviceId = group.getDeviceId();
        if (dsl().fetchExists(table(), baseCondition(group).and(table().DEVICE_ID.eq(deviceId)))) {
            throw FolderGroupMetadata.INSTANCE.alreadyExisted(
                baseExistedLog(group) + " and network_id=" + group.getNetworkId() + " and device_id=" + deviceId);
        }
        return group;
    }

}
