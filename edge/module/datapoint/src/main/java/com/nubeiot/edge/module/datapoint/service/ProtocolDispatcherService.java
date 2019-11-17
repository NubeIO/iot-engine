package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ProtocolDispatcherMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;

import lombok.NonNull;

public final class ProtocolDispatcherService
    extends AbstractEntityService<ProtocolDispatcher, ProtocolDispatcherMetadata>
    implements DataPointService<ProtocolDispatcher, ProtocolDispatcherMetadata> {

    public ProtocolDispatcherService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.GET_LIST);
    }

    public Set<EventMethodDefinition> definitions() {
        return Collections.singleton(
            EventMethodDefinition.create(servicePath(), context().requestKeyName(), ActionMethodMapping.READ_MAP));
    }

    @Override
    public ProtocolDispatcherMetadata context() {
        return ProtocolDispatcherMetadata.INSTANCE;
    }

}
