package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.AbstractOneToManyEntityService;
import com.nubeiot.core.sql.service.EntityPostService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.service.PointService.PointExtension;
import com.nubeiot.edge.module.datapoint.sync.PointValueSyncService;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

import lombok.NonNull;

public final class PointValueService extends AbstractOneToManyEntityService<PointValueData, PointValueMetadata>
    implements DataPointService<PointValueData, PointValueMetadata>, PointExtension {

    public PointValueService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public PointValueMetadata context() {
        return PointValueMetadata.INSTANCE;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_ONE, EventAction.CREATE, EventAction.PATCH, EventAction.REMOVE);
    }

    @Override
    public @NonNull ReferenceQueryExecutor<PointValueData> queryExecutor() {
        return super.queryExecutor();
    }

    @Override
    public @NonNull EntityPostService asyncPostService() {
        return new PointValueSyncService(DataPointService.super.asyncPostService());
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(super.onCreatingOneResource(requestData));
    }

    @Override
    public @NonNull RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(super.onModifyingOneResource(requestData));
    }

    @NonNull
    private RequestData recomputeRequestData(RequestData reqData) {
        JsonObject filter = Optional.ofNullable(reqData.getFilter()).orElse(new JsonObject()).put(Filters.AUDIT, true);
        return RequestData.builder()
                          .headers(reqData.headers())
                          .body(reqData.body())
                          .filter(filter)
                          .sort(reqData.getSort())
                          .pagination(reqData.getPagination())
                          .build();
    }

}
