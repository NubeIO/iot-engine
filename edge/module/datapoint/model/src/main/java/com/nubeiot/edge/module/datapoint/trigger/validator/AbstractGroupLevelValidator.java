package com.nubeiot.edge.module.datapoint.trigger.validator;

import org.jooq.Condition;
import org.jooq.DSLContext;

import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderGroupMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.FolderGroup;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
abstract class AbstractGroupLevelValidator implements GroupLevelValidator {

    @NonNull
    private final DSLContext dsl;

    @NonNull
    protected com.nubeiot.iotdata.edge.model.tables.FolderGroup table() {
        return FolderGroupMetadata.INSTANCE.table();
    }

    @NonNull
    protected Condition baseCondition(@NonNull FolderGroup group) {
        return table().LEVEL.eq(group.getLevel()).and(table().FOLDER_ID.eq(group.getFolderId()));
    }

}
