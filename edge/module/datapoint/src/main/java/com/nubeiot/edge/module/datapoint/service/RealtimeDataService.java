package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService.ReferenceEntityTransformer;
import com.nubeiot.edge.module.datapoint.service.Metadata.RealtimeDataMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;

import lombok.NonNull;

public final class RealtimeDataService
    extends AbstractDataPointService<PointRealtimeData, RealtimeDataMetadata, RealtimeDataService>
    implements OneToManyReferenceEntityService<PointRealtimeData, RealtimeDataMetadata, RealtimeDataService>,
               ReferenceEntityTransformer, PointExtension {

    public RealtimeDataService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public String servicePath() {
        return "realtime-data";
    }

    @Override
    public RealtimeDataMetadata metadata() {
        return RealtimeDataMetadata.INSTANCE;
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
    public @NonNull ReferenceQueryExecutor<PointRealtimeData> queryExecutor() {
        return OneToManyReferenceEntityService.super.queryExecutor();
    }

}
