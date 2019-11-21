package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class TransducerDeviceServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Device_Equip_Thing();
    }

    @Test
    public void test_get_list_transducer_by_device(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"transducers\":[{\"id\":1,\"transducer\":{\"id\":\"" + PrimaryKey.TRANS_HUMIDITY + "\"," +
            "\"code\":\"HUMIDITY_01\",\"type\":\"SENSOR\",\"category\":\"HUMIDITY\"}," +
            "\"product_code\":\"DROPLET-2CB2B763-H\",\"product_label\":{\"label\":\"Droplet Humidity\"}," +
            "\"measure_unit\":\"percentage\"},{\"id\":2,\"transducer\":{\"id\":\"" + PrimaryKey.TRANS_TEMP +
            "\",\"code\":\"TEMP_01\",\"type\":\"SENSOR\",\"category\":\"TEMP\"}," +
            "\"product_code\":\"DROPLET-2CB2B763-T\",\"product_label\":{\"label\":\"Droplet Temp\"}," +
            "\"measure_unit\":\"celsius\"}]}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE_DROPLET.toString()))
                                     .build();
        asserter(context, true, expected, TransducerByDevice.class.getName(), EventAction.GET_LIST, req);
    }

}
