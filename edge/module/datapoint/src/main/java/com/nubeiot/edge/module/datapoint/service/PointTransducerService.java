package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.decorator.RequestDecorator;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractManyToManyEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointTransducerMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.PointTransducerComposite;
import com.nubeiot.edge.module.datapoint.service.extension.NetworkExtension;
import com.nubeiot.iotdata.edge.model.tables.PointTransducer;

import lombok.NonNull;

abstract class PointTransducerService
    extends AbstractManyToManyEntityService<PointTransducerComposite, PointTransducerMetadata>
    implements DataPointService<PointTransducerComposite, PointTransducerMetadata> {

    PointTransducerService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull RequestDecorator requestDecorator() {
        return NetworkExtension.create(this);
    }

    @Override
    public Set<String> ignoreFields() {
        final @NonNull PointTransducer table = context().table();
        return Stream.of(super.ignoreFields(),
                         Arrays.asList(table.getJsonField(table.DEVICE_ID), table.getJsonField(table.NETWORK_ID),
                                       table.getJsonField(table.EDGE_ID),
                                       table.getJsonField(table.COMPUTED_TRANSDUCER)))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toSet());
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(getAvailableEvents(), resource(), reference());
    }

}
