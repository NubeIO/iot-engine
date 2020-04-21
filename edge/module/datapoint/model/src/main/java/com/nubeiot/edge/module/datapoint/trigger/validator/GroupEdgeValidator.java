package com.nubeiot.edge.module.datapoint.trigger.validator;

import org.jooq.DSLContext;

import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;

import lombok.NonNull;

public final class GroupEdgeValidator extends AbstractGroupLevelValidator {

    public GroupEdgeValidator(@NonNull DSLContext dsl) {
        super(dsl);
    }

    @Override
    public @NonNull FolderRef computeReference(@NonNull FolderGroup group) {
        if (!dsl().fetchExists(NetworkMetadata.INSTANCE.table(),
                               NetworkMetadata.INSTANCE.table().ID.eq(group.getNetworkId()))) {
            throw NetworkMetadata.INSTANCE.notFound(group.getNetworkId());
        }
        return FolderRef.builder().networkId(group.getNetworkId()).build();
    }

    @Override
    public @NonNull FolderGroup validateExisted(@NonNull FolderGroup group) {
        if (dsl().fetchExists(table(), baseCondition(group).and(table().NETWORK_ID.eq(group.getNetworkId())))) {
            throw new AlreadyExistException("Folder id " + group.getFolderId() + " is already existed");
        }
        return group;
    }

}
