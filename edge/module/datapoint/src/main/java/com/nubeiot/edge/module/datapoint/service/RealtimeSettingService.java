package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService.ReferenceEntityTransformer;
import com.nubeiot.edge.module.datapoint.service.Metadata.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;

import lombok.NonNull;

public final class RealtimeSettingService
    extends AbstractDataPointService<RealtimeSettingMetadata, RealtimeSettingService> implements
                                                                                      ReferenceEntityTransformer,
                                                                                      PointExtension,
                                                                                      OneToManyReferenceEntityService<RealtimeSettingMetadata, RealtimeSettingService> {

    public RealtimeSettingService(@NonNull AbstractEntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public RealtimeSettingMetadata metadata() {
        return RealtimeSettingMetadata.INSTANCE;
    }

    @Override
    public HasReferenceResource ref() {
        return this;
    }

    @Override
    public @NonNull ReferenceEntityTransformer transformer() {
        return this;
    }

    @Override
    public @NonNull ReferenceQueryExecutor queryExecutor() {
        return OneToManyReferenceEntityService.super.queryExecutor();
    }

}
