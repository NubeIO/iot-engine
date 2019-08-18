package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService.ReferenceEntityTransformer;
import com.nubeiot.edge.module.datapoint.service.Metadata.RealtimeSettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.RealtimeSetting;

import lombok.NonNull;

public final class RealtimeSettingService
    extends AbstractDataPointService<RealtimeSetting, RealtimeSettingMetadata, RealtimeSettingService>
    implements ReferenceEntityTransformer, PointExtension,
               OneToManyReferenceEntityService<RealtimeSetting, RealtimeSettingMetadata, RealtimeSettingService> {

    public RealtimeSettingService(@NonNull EntityHandler entityHandler) {
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
    public @NonNull ReferenceQueryExecutor<RealtimeSetting> queryExecutor() {
        return OneToManyReferenceEntityService.super.queryExecutor();
    }

}
