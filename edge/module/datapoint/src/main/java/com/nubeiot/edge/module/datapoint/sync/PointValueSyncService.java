package com.nubeiot.edge.module.datapoint.sync;

import java.time.OffsetDateTime;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.query.QueryBuilder;
import com.nubeiot.core.sql.query.SimpleQueryExecutor;
import com.nubeiot.core.sql.service.EntityPostService;
import com.nubeiot.core.sql.service.EntityPostService.EntityPostServiceDelegate;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.module.datapoint.model.ditto.DittoHistoryData;
import com.nubeiot.edge.module.datapoint.model.ditto.DittoPointData;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.service.HistoryDataService;
import com.nubeiot.edge.module.datapoint.service.PointValueService;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;
import com.nubeiot.iotdata.edge.model.tables.records.PointHistoryDataRecord;

import lombok.NonNull;

public final class PointValueSyncService extends EntityPostServiceDelegate {

    @SuppressWarnings("unchecked")
    public PointValueSyncService(@NonNull EntityPostService delegate) {
        super(delegate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSuccess(EntityService service, EventAction action, VertxPojo data) {
        if (!(service instanceof PointValueService) || (action != EventAction.CREATE && action != EventAction.PATCH)) {
            return;
        }
        final PointValueService vService = (PointValueService) service;
        final PointValueData pointValue = (PointValueData) data;
        final OffsetDateTime createdTime = action == EventAction.CREATE
                                           ? pointValue.getTimeAudit().getCreatedTime()
                                           : pointValue.getTimeAudit().getLastModifiedTime();
        final PointHistoryData historyData = new PointHistoryData().setPoint(pointValue.getPoint())
                                                                   .setValue(pointValue.getValue())
                                                                   .setPriority(pointValue.getPriority())
                                                                   .setTime(createdTime);
        final Single<PointHistoryData> history = createHistory(historyData,
                                                               SimpleQueryExecutor.create(vService.entityHandler(),
                                                                                          HistoryDataMetadata.INSTANCE));
        history.toFlowable()
               .flatMap(
                   his -> Maybe.concat(getDelegate().doSyncOnSuccess(service, action, new DittoPointData(pointValue)),
                                       getDelegate().doSyncOnSuccess(new HistoryDataService(service.entityHandler()),
                                                                     action, new DittoHistoryData(his))))
               .subscribe();
    }

    @SuppressWarnings("unchecked")
    private Single<PointHistoryData> createHistory(PointHistoryData historyData, SimpleQueryExecutor executor) {
        JsonObject filter = new JsonObject().put("point", historyData.getPoint().toString())
                                            .put("time", DateTimes.format(historyData.getTime()));
        final @NonNull JsonTable<PointHistoryDataRecord> table = HistoryDataMetadata.INSTANCE.table();
        final QueryBuilder queryBuilder = executor.queryBuilder();
        return executor.fetchExists(queryBuilder.exist(table, queryBuilder.condition(table, filter)))
                       .switchIfEmpty(executor.insertReturningPrimary(historyData, RequestData.builder().build()))
                       .flatMap(executor::lookupByPrimaryKey);
    }

}
