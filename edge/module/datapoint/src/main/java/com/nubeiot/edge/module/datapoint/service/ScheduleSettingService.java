package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService.ReferenceEntityTransformer;
import com.nubeiot.edge.module.datapoint.service.Metadata.SchedulerSettingMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.ScheduleSetting;

import lombok.NonNull;

public final class ScheduleSettingService
    extends AbstractDataPointService<ScheduleSetting, SchedulerSettingMetadata, ScheduleSettingService>
    implements ReferenceEntityTransformer, PointExtension,
               OneToManyReferenceEntityService<ScheduleSetting, SchedulerSettingMetadata, ScheduleSettingService> {

    public ScheduleSettingService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public String servicePath() {
        return "/schedules";
    }

    @Override
    public SchedulerSettingMetadata metadata() {
        return SchedulerSettingMetadata.INSTANCE;
    }

    @Override
    public HasReferenceResource ref() {
        return this;
    }

    @Override
    public ReferenceEntityTransformer transformer() {
        return this;
    }

    @Override
    public @NonNull ReferenceQueryExecutor<ScheduleSetting> queryExecutor() {
        return OneToManyReferenceEntityService.super.queryExecutor();
    }

}
