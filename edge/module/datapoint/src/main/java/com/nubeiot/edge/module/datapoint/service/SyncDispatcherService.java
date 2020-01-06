package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.workflow.task.EntityTask;
import com.nubeiot.edge.module.datapoint.DataPointIndex.SyncDispatcherMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.SyncDispatcher;

import lombok.NonNull;

public final class SyncDispatcherService extends AbstractEntityService<SyncDispatcher, SyncDispatcherMetadata>
    implements DataPointService<SyncDispatcher, SyncDispatcherMetadata> {

    public SyncDispatcherService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.GET_LIST, EventAction.CREATE, EventAction.UPDATE);
    }

    @Override
    public EntityTask prePersistTask() {
        return null;
    }

    public Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(ActionMethodMapping.DQL_MAP, this::servicePath,
                                                   context()::requestKeyName, false);
    }

    @Override
    public SyncDispatcherMetadata context() {
        return SyncDispatcherMetadata.INSTANCE;
    }

}
