package com.nubeiot.edge.module.datapoint.sync;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.client.HttpClientConfig;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.query.SimpleQueryExecutor;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.HistoryDataMetadata;
import com.nubeiot.edge.module.datapoint.service.PointValueService;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;
import com.nubeiot.iotdata.edge.model.tables.records.PointHistoryDataRecord;

import lombok.NonNull;

public final class PointValueSyncService extends DittoHttpSync {

    public PointValueSyncService(Vertx vertx) {
        super(vertx, new HttpClientConfig());
    }

    @Override
    public void onSuccess(EntityService service, EventAction action, JsonObject data) {
        if (!(service instanceof PointValueService) || action != EventAction.CREATE) {
            return;
        }
        final PointValueService vService = (PointValueService) service;
        final PointValueData pointValue = vService.context().parseFromRequest(EntityTransformer.getData(data));
        final PointHistoryData historyData = new PointHistoryData().setPoint(pointValue.getPoint())
                                                                   .setValue(pointValue.getValue())
                                                                   .setPriority(pointValue.getPriority())
                                                                   .setTime(pointValue.getTimeAudit().getCreatedTime())
                                                                   .setTimeAudit(pointValue.getTimeAudit())
                                                                   .setSyncAudit(pointValue.getSyncAudit());
        createHistory(historyData, SimpleQueryExecutor.create(vService.entityHandler(), HistoryDataMetadata.INSTANCE));
    }

    @SuppressWarnings("unchecked")
    private void createHistory(PointHistoryData historyData, SimpleQueryExecutor executor) {
        JsonObject filter = new JsonObject().put("point", historyData.getPoint().toString())
                                            .put("time", DateTimes.format(historyData.getTimeAudit().getCreatedTime()));
        final @NonNull JsonTable<PointHistoryDataRecord> table = HistoryDataMetadata.INSTANCE.table();
        executor.fetchExists(executor.existQuery(table, executor.condition(table, filter)))
                .switchIfEmpty(executor.insertReturningPrimary(historyData, RequestData.builder().build()))
                .subscribe();
    }

}
