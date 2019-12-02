package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ProtocolDispatcherMetadata;
import com.nubeiot.edge.module.datapoint.task.remote.ProtocolDispatcherTask;
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
        return Arrays.asList(EventAction.GET_ONE, EventAction.GET_LIST, EventAction.CREATE, EventAction.UPDATE);
    }

    @Override
    public Optional<ProtocolDispatcherTask> prePersistTask() {
        return Optional.empty();
    }

    public Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(ActionMethodMapping.DQL_MAP, this::servicePath,
                                                   context()::requestKeyName);
    }

    @Override
    public ProtocolDispatcherMetadata context() {
        return ProtocolDispatcherMetadata.INSTANCE;
    }

}
