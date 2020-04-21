package com.nubeiot.edge.module.datapoint.trigger.validator;

import java.util.UUID;

import org.jooq.DSLContext;

import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public interface GroupLevelValidator {

    @NonNull DSLContext dsl();

    @NonNull FolderRef computeReference(@NonNull FolderGroup group);

    @NonNull FolderGroup validateExisted(@NonNull FolderGroup group);

    default @NonNull FolderGroup compute(@NonNull FolderGroup group) {
        final FolderRef fr = computeReference(group);
        return validateExisted(group.setNetworkId(fr.networkId()).setDeviceId(fr.deviceId()).setPointId(fr.pointId()));
    }

    interface GroupLevelValidatorDecorator extends GroupLevelValidator {

        @NonNull GroupLevelValidator initDecorator(@NonNull DSLContext dsl);

        boolean shouldDecorator(@NonNull FolderGroup group);

        default @NonNull FolderGroup selfCompute(@NonNull FolderGroup group) {
            return GroupLevelValidator.super.compute(group);
        }

        @Override
        default @NonNull FolderGroup compute(@NonNull FolderGroup group) {
            return shouldDecorator(group) ? initDecorator(dsl()).compute(group) : selfCompute(group);
        }

    }


    @Getter
    @Builder
    @Accessors(fluent = true)
    final class FolderRef {

        private final UUID networkId;
        private final UUID deviceId;
        private final UUID pointId;

    }

}
