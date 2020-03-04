package com.nubeiot.edge.module.datapoint.service;

import java.util.Set;

import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.http.EntityHttpService;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.core.sql.workflow.task.EntityTask;
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
    public EntityTask prePersistTask() {
        return null;
    }

    @Override
    public EntityTask postPersistAsyncTask() {
        return null;
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return EntityHttpService.createDefinitions(getAvailableEvents(), this::servicePath, context()::requestKeyName);
    }

    @Override
    public String servicePath() {
        return Urls.toPathWithLC(context().modelClass().getSimpleName());
    }

    //    @Override
    //    public Single<JsonObject> afterCreate(Object key, @NonNull VertxPojo pojo, @NonNull RequestData reqData) {
    //        return Single.error(new RuntimeException("After Create"));
    //    }
}
