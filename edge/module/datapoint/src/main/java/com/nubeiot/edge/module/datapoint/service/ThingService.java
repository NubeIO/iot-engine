package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractTransitiveEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeDeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ThingMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Thing;

import lombok.NonNull;

public final class ThingService extends AbstractTransitiveEntityService<Thing, ThingMetadata>
    implements DataPointService<Thing, ThingMetadata> {

    public ThingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public ThingMetadata context() {
        return ThingMetadata.INSTANCE;
    }

    @Override
    public EntityReferences entityReferences() {
        return new EntityReferences().add(DeviceMetadata.INSTANCE);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Stream.concat(DataPointService.super.definitions().stream(),
                             EntityHttpService.createDefinitions(getAvailableEvents(), context(),
                                                                 DeviceMetadata.INSTANCE).stream())
                     .collect(Collectors.toSet());
    }

    @Override
    public @NonNull Map<EntityMetadata, TransitiveEntity> transitiveReferences() {
        final TransitiveEntity transitive = new TransitiveEntity(EdgeDeviceMetadata.INSTANCE,
                                                                 new EntityReferences().add(NetworkMetadata.INSTANCE));
        return Collections.singletonMap(DeviceMetadata.INSTANCE, transitive);
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return super.onCreatingOneResource(optimizeRequestData(requestData));
    }

    @Override
    public @NonNull RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        return super.onModifyingOneResource(optimizeRequestData(requestData));
    }

    @Override
    public @NonNull RequestData onReadingManyResource(@NonNull RequestData requestData) {
        return super.onReadingManyResource(optimizeRequestData(requestData));
    }

    @Override
    public @NonNull RequestData onReadingOneResource(@NonNull RequestData requestData) {
        return super.onReadingOneResource(optimizeRequestData(requestData));
    }

    private RequestData optimizeRequestData(@NonNull RequestData requestData) {
        DataPointIndex.NetworkMetadata.optimizeAlias(requestData.body());
        DataPointIndex.NetworkMetadata.optimizeAlias(requestData.filter());
        return requestData;
    }

}
