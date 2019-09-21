package com.nubeiot.edge.module.datapoint.sync;

import java.time.OffsetDateTime;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.sql.service.EntityPostService;
import com.nubeiot.core.sql.service.EntityPostService.EntityPostServiceDelegate;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.RealtimeDataMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointService;
import com.nubeiot.edge.module.datapoint.service.HistoryDataService;
import com.nubeiot.edge.module.datapoint.service.PointValueService;
import com.nubeiot.edge.module.datapoint.service.RealtimeDataService;
import com.nubeiot.iotdata.dto.PointPriorityValue.PointValue;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointHistoryData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointRealtimeData;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

import lombok.NonNull;

public final class PointValueSyncService extends EntityPostServiceDelegate {

    @SuppressWarnings("unchecked")
    public PointValueSyncService(@NonNull EntityPostService delegate) {
        super(delegate);
    }

    @Override
    public void onSuccess(EntityService service, EventAction action, VertxPojo data, @NonNull RequestData requestData) {
        if (!(service instanceof PointValueService)) {
            return;
        }
        if (action == EventAction.CREATE || action == EventAction.PATCH) {
            final PointValueData pointValue = (PointValueData) data;
            final OffsetDateTime createdTime = action == EventAction.CREATE
                                               ? pointValue.getTimeAudit().getCreatedTime()
                                               : pointValue.getTimeAudit().getLastModifiedTime();
            final PointValueData reqValue = PointValueMetadata.INSTANCE.parseFromRequest(requestData.body());
            final PointValue requestValue = new PointValue(reqValue.getPriority(), reqValue.getValue());
            createHistoryData(service, pointValue, requestValue, createdTime);
            createRealtimeData(service, pointValue, requestValue, createdTime);
        }
        getDelegate().onSuccess(service, action, data, requestData);
    }

    private void createRealtimeData(@NonNull EntityService service, @NonNull PointValueData pv,
                                    @NonNull PointValue requestValue, @NonNull OffsetDateTime createdTime) {
        final JsonObject rtValue = RealtimeDataMetadata.simpleValue(requestValue.getValue(),
                                                                    requestValue.getPriority());
        send(service, RealtimeDataService.class,
             new PointRealtimeData().setPoint(pv.getPoint()).setValue(rtValue).setTime(createdTime).toJson());
    }

    private void createHistoryData(@NonNull EntityService service, @NonNull PointValueData pv,
                                   @NonNull PointValue requestValue, @NonNull OffsetDateTime createdTime) {
        send(service, HistoryDataService.class, new PointHistoryData().setPoint(pv.getPoint())
                                                                      .setValue(requestValue.getValue())
                                                                      .setPriority(requestValue.getPriority())
                                                                      .setTime(createdTime)
                                                                      .toJson());
    }

    private void send(@NonNull EntityService service, @NonNull Class<? extends DataPointService> serviceName,
                      @NonNull JsonObject body) {
        service.entityHandler()
               .eventClient()
               .request(DeliveryEvent.builder()
                                     .address(serviceName.getName())
                                     .action(EventAction.CREATE)
                                     .pattern(EventPattern.POINT_2_POINT)
                                     .addPayload(RequestData.builder().body(body).build())
                                     .build());
    }

}
