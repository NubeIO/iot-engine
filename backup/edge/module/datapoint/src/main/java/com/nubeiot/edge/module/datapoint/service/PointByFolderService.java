package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.iotdata.dto.GroupLevel;

import lombok.NonNull;

public final class PointByFolderService extends FolderGroupService {

    public PointByFolderService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return FolderMetadata.INSTANCE;
    }

    @Override
    public @NonNull List<EntityMetadata> references() {
        return Arrays.asList(FolderMetadata.INSTANCE, DeviceMetadata.INSTANCE, NetworkMetadata.INSTANCE);
    }

    @Override
    public @NonNull EntityMetadata resource() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        return Stream.of(super.definitions(),
                         EntityHttpService.createDefinitions(getAvailableEvents(), resource(), true,
                                                             DeviceMetadata.INSTANCE, reference()),
                         EntityHttpService.createDefinitions(getAvailableEvents(), resource(), true,
                                                             NetworkMetadata.INSTANCE, DeviceMetadata.INSTANCE,
                                                             reference()))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    @Override
    protected @NonNull GroupLevel groupLevel() {
        return GroupLevel.DEVICE;
    }

}
