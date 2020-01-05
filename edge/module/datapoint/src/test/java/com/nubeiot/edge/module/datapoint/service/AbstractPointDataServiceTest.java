package com.nubeiot.edge.module.datapoint.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.PointPriorityValue;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

public abstract class AbstractPointDataServiceTest extends BaseDataPointServiceTest {

    protected final PointValueData firstValue = new PointValueData().setPoint(PrimaryKey.P_GPIO_TEMP)
                                                                    .setPriority(5)
                                                                    .setValue(28d);

    @Override
    protected void setup(TestContext context) {
        super.setup(context);
        final RequestData msg = RequestData.builder()
                                           .body(new JsonObject().put("enabled", true)
                                                                 .put("point_id",
                                                                      UUID64.uuidToBase64(PrimaryKey.P_GPIO_TEMP)))
                                           .build();
        CountDownLatch latch = new CountDownLatch(1);
        controller().request(settingAddress(), EventMessage.initial(EventAction.CREATE_OR_UPDATE, msg))
                    .doOnSuccess(ignore -> {
                        final PointValueData output = new PointValueData(firstValue).setPriorityValues(
                            new PointPriorityValue().add(5, 28));
                        PointValueServiceTest.createPointValue(controller(), context, EventAction.CREATE, firstValue,
                                                               output);
                        latch.countDown();
                    })
                    .subscribe();
        try {
            latch.await(3000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            context.fail(new RuntimeException("Timeout when updating history_setting", e));
        }
    }

    @Override
    protected final JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    protected abstract String settingAddress();

}
