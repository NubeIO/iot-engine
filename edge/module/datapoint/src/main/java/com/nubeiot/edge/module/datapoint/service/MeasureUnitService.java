package com.nubeiot.edge.module.datapoint.service;

import java.util.Optional;

import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.AbstractEntityService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.MeasureUnitMetadata;
import com.nubeiot.edge.module.datapoint.task.remote.ProtocolDispatcherTask;
import com.nubeiot.edge.module.datapoint.task.sync.SyncTask;
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
    public String servicePath() {
        return Urls.toPathWithLC(context().modelClass().getSimpleName());
    }

    @Override
    public Optional<ProtocolDispatcherTask> taskBeforePersist() {
        return Optional.empty();
    }

    @Override
    public Optional<SyncTask> asyncTaskAfterPersist() {
        return Optional.empty();
    }

}
