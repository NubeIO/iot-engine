package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.github.zero88.utils.UUID64;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class TransducerByPointServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_transducers_by_point(TestContext context) {
        final JsonObject transducer = new JsonObject(
            "{\"transducers\":[{\"id\":1,\"transducer\":{\"id\":\"" + PrimaryKey.TRANSDUCER_HUMIDITY_DROPLET +
            "\",\"code\":" +
            "\"DROPLET-2CB2B763-H\",\"type\":\"SENSOR\",\"category\":\"HUMIDITY\",\"label\":{\"label\":\"Droplet " +
            "Humidity\"},\"measure_unit\":\"percentage\"}}]}");
        final JsonObject reqBody = new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_GPIO_HUMIDITY));
        asserter(context, true, transducer, TransducerByPointService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().body(reqBody).build());
    }

    @Test
    public void test_get_transducer_not_assign_to_point(TestContext context) {
        final JsonObject transducer = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                      .put("message", "Not found resource with point_id=" +
                                                                      PrimaryKey.P_GPIO_HUMIDITY +
                                                                      " and transducer_id=" +
                                                                      PrimaryKey.TRANSDUCER_TEMP_HVAC);
        final JsonObject reqBody = new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_GPIO_HUMIDITY))
                                                   .put("transducer_id",
                                                        UUID64.uuidToBase64(PrimaryKey.TRANSDUCER_TEMP_HVAC));
        asserter(context, false, transducer, TransducerByPointService.class.getName(), EventAction.GET_ONE,
                 RequestData.builder().body(reqBody).build());
    }

}
