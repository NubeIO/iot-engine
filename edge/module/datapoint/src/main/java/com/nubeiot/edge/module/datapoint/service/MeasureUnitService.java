package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;

import io.github.zero.utils.Urls;

import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.workflow.task.EntityTaskManager;
import com.nubeiot.core.sql.workflow.task.EntityTaskManagerImpl;
import com.nubeiot.edge.module.datapoint.DataPointIndex.MeasureUnitMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.MeasureUnit;

import lombok.NonNull;

public final class MeasureUnitService extends AbstractEntityService<MeasureUnit, MeasureUnitMetadata>
    implements DataPointService<MeasureUnit, MeasureUnitMetadata> {

    public MeasureUnitService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public MeasureUnitMetadata context() {
        return MeasureUnitMetadata.INSTANCE;
    }

    @Override
    public @NonNull EntityTaskManager taskManager() {
        return EntityTaskManagerImpl.builder().postPersistTask(super.taskManager().postPersistTask()).build();
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(getAvailableEvents(), this::servicePath, context()::requestKeyName);
    }

    @Override
    public String servicePath() {
        return Urls.toPathWithLC(context().modelClass().getSimpleName());
    }

}
