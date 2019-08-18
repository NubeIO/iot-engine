package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService.ReferenceEntityTransformer;
import com.nubeiot.edge.module.datapoint.service.Metadata.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;

import lombok.NonNull;

public final class HistoryDataService
    extends AbstractDataPointService<PointHistoryData, HistoryDataMetadata, HistoryDataService>
    implements OneToManyReferenceEntityService<PointHistoryData, HistoryDataMetadata, HistoryDataService>,
               ReferenceEntityTransformer, PointExtension {

    public HistoryDataService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public String servicePath() {
        return "/histories";
    }

    @Override
    public HistoryDataMetadata metadata() {
        return HistoryDataMetadata.INSTANCE;
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
    public @NonNull ReferenceQueryExecutor<PointHistoryData> queryExecutor() {
        return OneToManyReferenceEntityService.super.queryExecutor();
    }

}
