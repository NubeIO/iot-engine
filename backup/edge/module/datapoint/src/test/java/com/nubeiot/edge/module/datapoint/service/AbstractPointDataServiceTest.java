package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;

import lombok.NonNull;

public abstract class AbstractPointDataServiceTest extends BaseDataPointServiceTest {

    @Override
    protected final JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    protected final Single<EventMessage> enabledSettingThenAddPV(@NonNull TestContext context, JsonObject setting,
                                                                 @NonNull DeliveryEvent pointValueEvent) {
        return controller().request(settingAddress(), EventMessage.initial(EventAction.CREATE_OR_UPDATE,
                                                                           RequestData.builder().body(setting).build()))
                           .filter(EventMessage::isSuccess)
                           .switchIfEmpty(
                               Single.error(new RuntimeException("Timeout when updating " + settingAddress())))
                           .flatMap(ignore -> addPV(pointValueEvent))
                           .doOnError(context::fail)
                           .delay(1, TimeUnit.SECONDS);
    }

    protected final Single<EventMessage> addPV(@NonNull DeliveryEvent pointValueEvent) {
        return controller().request(pointValueEvent)
                           .filter(EventMessage::isSuccess)
                           .switchIfEmpty(Single.error(new RuntimeException(
                               "Failed when add point value " + pointValueEvent.payload().getData())));
    }

    protected final Single<EventMessage> doQuery(@NonNull UUID point) {
        final JsonObject req = new JsonObject().put("point_id", point.toString());
        return controller().request(DeliveryEvent.builder()
                                                 .address(dataAddress())
                                                 .action(EventAction.GET_LIST)
                                                 .payload(RequestData.builder().body(req).build().toJson())
                                                 .build());
    }

    protected abstract String settingAddress();

    protected abstract String dataAddress();

}
