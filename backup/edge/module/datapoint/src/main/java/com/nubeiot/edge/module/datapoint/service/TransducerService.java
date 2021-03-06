package com.nubeiot.edge.module.datapoint.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.RequestDecorator;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractTransitiveEntityService;
import com.nubeiot.core.sql.service.marker.EntityReferences;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeDeviceMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.TransducerMetadata;
import com.nubeiot.edge.module.datapoint.service.extension.NetworkExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.Transducer;

import lombok.NonNull;

public final class TransducerService extends AbstractTransitiveEntityService<Transducer, TransducerMetadata>
    implements DataPointService<Transducer, TransducerMetadata> {

    public TransducerService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public TransducerMetadata context() {
        return TransducerMetadata.INSTANCE;
    }

    @Override
    public EntityReferences referencedEntities() {
        return new EntityReferences().add(DeviceMetadata.INSTANCE);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Stream.of(DataPointService.super.definitions(),
                         EntityHttpService.createDefinitions(getAvailableEvents(), context(), DeviceMetadata.INSTANCE),
                         EntityHttpService.createDefinitions(getAvailableEvents(), context(), true,
                                                             NetworkMetadata.INSTANCE, DeviceMetadata.INSTANCE))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    @Override
    public @NonNull Map<EntityMetadata, TransitiveEntity> transitiveReferences() {
        final TransitiveEntity transitive = new TransitiveEntity(EdgeDeviceMetadata.INSTANCE,
                                                                 new EntityReferences().add(NetworkMetadata.INSTANCE));
        return Collections.singletonMap(DeviceMetadata.INSTANCE, transitive);
    }

    @Override
    public @NonNull RequestDecorator requestDecorator() {
        return NetworkExtension.create(this);
    }

    @Override
    public boolean supportForceDeletion() {
        return true;
    }

}
