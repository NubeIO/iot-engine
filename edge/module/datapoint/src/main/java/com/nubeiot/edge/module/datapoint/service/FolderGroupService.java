package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.decorator.RequestDecorator;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderGroupMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.FolderGroupComposite;
import com.nubeiot.edge.module.datapoint.service.extension.NetworkExtension;
import com.nubeiot.iotdata.dto.GroupLevel;
import com.nubeiot.iotdata.edge.model.tables.Folder;
import com.nubeiot.iotdata.edge.model.tables.FolderGroup;

import lombok.NonNull;

public abstract class FolderGroupService
    extends AbstractManyToManyEntityService<FolderGroupComposite, FolderGroupMetadata>
    implements DataPointService<FolderGroupComposite, FolderGroupMetadata> {

    public FolderGroupService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public FolderGroupMetadata context() {
        return FolderGroupMetadata.INSTANCE;
    }

    @Override
    public @NonNull RequestDecorator requestDecorator() {
        return NetworkExtension.create(this);
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return appendGroupLevel(super.onCreatingOneResource(requestData));
    }

    @Override
    public @NonNull RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        return appendGroupLevel(super.onModifyingOneResource(requestData));
    }

    @Override
    public @NonNull RequestData onDeletingOneResource(@NonNull RequestData requestData) {
        return appendGroupLevel(super.onDeletingOneResource(requestData));
    }

    @Override
    public @NonNull RequestData onReadingManyResource(@NonNull RequestData requestData) {
        return appendGroupLevel(super.onReadingManyResource(requestData));
    }

    @Override
    public @NonNull RequestData onReadingOneResource(@NonNull RequestData requestData) {
        return appendGroupLevel(super.onReadingOneResource(requestData));
    }

    @Override
    public Set<String> ignoreFields() {
        final FolderGroup fgTbl = context().table();
        final Folder folderTbl = FolderMetadata.INSTANCE.table();
        return Stream.of(super.ignoreFields(),
                         Arrays.asList(fgTbl.getJsonField(fgTbl.NETWORK_ID), fgTbl.getJsonField(fgTbl.DEVICE_ID),
                                       fgTbl.getJsonField(fgTbl.FOLDER_ID), fgTbl.getJsonField(fgTbl.PARENT_FOLDER_ID),
                                       fgTbl.getJsonField(fgTbl.POINT_ID), fgTbl.getJsonField(fgTbl.LEVEL),
                                       folderTbl.getJsonField(folderTbl.EDGE_ID)))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(getAvailableEvents(), resource(), reference());
    }

    @NonNull
    protected abstract GroupLevel groupLevel();

    @NonNull
    private RequestData appendGroupLevel(@NonNull RequestData reqData) {
        reqData.body().put(context().table().getJsonField(context().table().LEVEL), groupLevel().type());
        reqData.filter().put(context().table().getJsonField(context().table().LEVEL), groupLevel().type());
        return reqData;
    }

}
