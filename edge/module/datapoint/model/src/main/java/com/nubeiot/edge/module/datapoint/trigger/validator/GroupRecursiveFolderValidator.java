package com.nubeiot.edge.module.datapoint.trigger.validator;

import java.util.Optional;

import org.jooq.DSLContext;

import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderGroupMetadata;
import com.nubeiot.iotdata.dto.GroupLevel;
import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;
import com.nubeiot.iotdata.edge.model.tables.records.FolderGroupRecord;

import lombok.NonNull;

public final class GroupRecursiveFolderValidator extends AbstractGroupLevelValidator {

    public GroupRecursiveFolderValidator(@NonNull DSLContext dsl) {
        super(dsl);
    }

    @Override
    public @NonNull FolderRef computeReference(@NonNull FolderGroup group) {
        return null;
    }

    @Override
    public @NonNull FolderGroup validateExisted(@NonNull FolderGroup group) {
        return null;
    }

    @Override
    public @NonNull FolderGroup compute(@NonNull FolderGroup group) {
        if (dsl().fetchExists(table(), table().LEVEL.eq(GroupLevel.FOLDER)
                                                    .and(table().FOLDER_ID.eq(group.getFolderId()))
                                                    .and(table().PARENT_FOLDER_ID.eq(group.getParentFolderId())))) {
            throw new AlreadyExistException(
                "Folder id " + group.getFolderId() + " is already defined in folder id " + group.getNetworkId());
        }
        final FolderGroupRecord record = dsl().selectFrom(table())
                                              .where(table().FOLDER_ID.eq(group.getParentFolderId())
                                                                      .and(table().LEVEL.eq(group.getLevel())))
                                              .limit(1)
                                              .fetchOne();
        final FolderGroup parent = Optional.ofNullable(record)
                                           .map(r -> r.into(FolderGroupMetadata.INSTANCE.modelClass()))
                                           .orElseThrow(() -> new NotFoundException(
                                               "Not found root folder id " + group.getParentFolderId()));
        return group.setNetworkId(parent.getNetworkId()).setDeviceId(parent.getDeviceId()).setPointId(null);
    }

}
