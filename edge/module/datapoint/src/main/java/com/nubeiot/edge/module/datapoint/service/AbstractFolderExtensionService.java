package com.nubeiot.edge.module.datapoint.service;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.zero.utils.UUID64;
import io.reactivex.Single;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.FolderGroupComposite;
import com.nubeiot.edge.module.datapoint.service.extension.FolderExtension;
import com.nubeiot.iotdata.dto.GroupLevel;
import com.nubeiot.iotdata.edge.model.tables.pojos.Folder;

import lombok.NonNull;

abstract class AbstractFolderExtensionService extends FolderGroupService implements FolderExtension {

    AbstractFolderExtensionService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return FolderExtension.super.resourceMetadata();
    }

    @Override
    protected OperationValidator initCreationValidator() {
        Supplier<UUID> edge = () -> UUID64.uuid64ToUuid(entityHandler().sharedData(DataPointIndex.EDGE_ID));
        return super.initCreationValidator().andThen(OperationValidator.create((reqData, pojo) -> {
            final Folder folder = ((FolderGroupComposite) pojo).getOther(FolderMetadata.INSTANCE.singularKeyName());
            Optional.ofNullable(folder).ifPresent(f -> f.setEdgeId(Optional.ofNullable(f.getEdgeId()).orElseGet(edge)));
            return Single.just(pojo);
        }));
    }

    @Override
    protected @NonNull GroupLevel groupLevel() {
        return GroupLevel.DEVICE;
    }

}
