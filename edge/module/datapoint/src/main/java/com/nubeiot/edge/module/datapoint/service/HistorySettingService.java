package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService.ReferenceEntityTransformer;
import com.nubeiot.edge.module.datapoint.service.Metadata.HistorySettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;

import lombok.NonNull;

public final class HistorySettingService extends AbstractDataPointService<HistorySettingMetadata, HistorySettingService>
    implements OneToManyReferenceEntityService<HistorySettingMetadata, HistorySettingService>,
               ReferenceEntityTransformer,
               PointExtension {

    public HistorySettingService(@NonNull AbstractEntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public HistorySettingMetadata metadata() {
        return HistorySettingMetadata.INSTANCE;
    }

    @Override
    public HasReferenceResource ref() {
        return this;
    }

    @Override
    public @NonNull OneToManyReferenceEntityService.ReferenceEntityTransformer transformer() {
        return this;
    }

    @Override
    public @NonNull ReferenceQueryExecutor queryExecutor() {
        return OneToManyReferenceEntityService.super.queryExecutor();
    }

}
