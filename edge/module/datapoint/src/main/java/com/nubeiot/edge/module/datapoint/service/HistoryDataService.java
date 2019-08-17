package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.HasReferenceResource;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService;
import com.nubeiot.core.sql.service.OneToManyReferenceEntityService.ReferenceEntityTransformer;
import com.nubeiot.edge.module.datapoint.service.Metadata.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;

import lombok.NonNull;

public final class HistoryDataService extends AbstractDataPointService<HistoryDataMetadata, HistoryDataService>
    implements OneToManyReferenceEntityService<HistoryDataMetadata, HistoryDataService>,
               ReferenceEntityTransformer,
               PointExtension {

    public HistoryDataService(@NonNull AbstractEntityHandler entityHandler) {
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
    public @NonNull ReferenceQueryExecutor queryExecutor() {
        return OneToManyReferenceEntityService.super.queryExecutor();
    }

}
