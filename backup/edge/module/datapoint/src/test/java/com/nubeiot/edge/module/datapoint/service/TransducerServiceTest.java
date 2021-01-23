package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class TransducerServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Device_Transducer();
    }

    @Test
    public void test_get_list_transducer_by_device(TestContext context) {
        final JsonObject transducer1 = new JsonObject("{\"id\":\"" + PrimaryKey.TRANSDUCER_TEMP_DROPLET +
                                                      "\",\"code\":\"DROPLET-2CB2B763-T\",\"type\":\"SENSOR\"," +
                                                      "\"category\":\"TEMP\",\"label\":{\"label\":\"Droplet Temp\"}," +
                                                      "\"measure_unit\":\"celsius\"}");
        final JsonObject transducer2 = new JsonObject(
            "{\"id\":\"" + PrimaryKey.TRANSDUCER_HUMIDITY_DROPLET + "\",\"code\":\"DROPLET-2CB2B763-H\"," +
            "\"type\":\"SENSOR\",\"category\":\"HUMIDITY\",\"label\":{\"label\":\"Droplet Humidity\"}," +
            "\"measure_unit\":\"percentage\"}");
        final JsonObject expected = new JsonObject().put("transducers",
                                                         new JsonArray().add(transducer1).add(transducer2));
        final RequestData req = RequestData.builder()
                                           .body(
                                               new JsonObject().put("device_id", PrimaryKey.DEVICE_DROPLET.toString()))
                                           .build();
        asserter(context, true, expected, TransducerService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_transducer_wrong_associate_with_device(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message", "Not found resource with transducer_id=" +
                                                              PrimaryKey.TRANSDUCER_FAN_HVAC);
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE_DROPLET.toString())
                                                           .put("transducer_id",
                                                                PrimaryKey.TRANSDUCER_FAN_HVAC.toString()))
                                     .build();
        asserter(context, false, expected, TransducerService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_transducer_correct_associate_with_device(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"code\":\"HVAC-XYZ-FAN\",\"id\":\"" + PrimaryKey.TRANSDUCER_FAN_HVAC + "\",\"category\":\"VELOCITY\"," +
            "\"label\":{\"label\":\"HVAC Fan\"},\"type\":\"SENSOR\",\"measure_unit\":\"revolutions_per_minute\"}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE_HVAC.toString())
                                                           .put("transducer_id",
                                                                PrimaryKey.TRANSDUCER_FAN_HVAC.toString()))
                                     .build();
        asserter(context, true, expected, TransducerService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_transducers_by_device_n_transitive_network(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"transducers\":[{\"id\":\"" + PrimaryKey.TRANSDUCER_TEMP_HVAC + "\",\"code\":\"HVAC-XYZ-TEMP-01\"," +
            "\"type\":\"SENSOR\",\"category\":\"TEMP\",\"label\":{\"label\":\"HVAC Temp\"}," +
            "\"measure_unit\":\"celsius\"},{\"id\":\"" + PrimaryKey.TRANSDUCER_FAN_HVAC + "\"," +
            "\"code\":\"HVAC-XYZ-FAN\",\"type\":\"SENSOR\",\"category\":\"VELOCITY\",\"label\":{\"label\":\"HVAC " +
            "Fan\"},\"measure_unit\":\"revolutions_per_minute\"},{\"id\":\"" + PrimaryKey.TRANSDUCER_SWITCH_HVAC +
            "\"," + "\"code\":\"HVAC-XYZ-FAN-CONTROL\",\"type\":\"ACTUATOR\",\"category\":\"SWITCH\"," +
            "\"label\":{\"label\":\"HVAC Fan Control\"},\"measure_unit\":\"bool\"}]}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE_HVAC.toString())
                                                           .put("network_id", PrimaryKey.BACNET_NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, TransducerService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_transducers_by_device_not_associated_transitive_network(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with network_id=" + PrimaryKey.BACNET_NETWORK +
            " and device_id=" + PrimaryKey.DEVICE_DROPLET + "\"}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE_DROPLET.toString())
                                                           .put("network_id", PrimaryKey.BACNET_NETWORK.toString()))
                                     .build();
        asserter(context, false, expected, TransducerService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_one_transducer_by_device_n_transitive_network(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"code\":\"HVAC-XYZ-TEMP-01\",\"id\":\"" + PrimaryKey.TRANSDUCER_TEMP_HVAC + "\"," +
            "\"label\":{\"label\":\"HVAC Temp\"},\"type\":\"SENSOR\",\"category\":\"TEMP\"," +
            "\"measure_unit\":\"celsius\"}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE_HVAC.toString())
                                                           .put("network_id", PrimaryKey.BACNET_NETWORK.toString())
                                                           .put("transducer_id",
                                                                PrimaryKey.TRANSDUCER_TEMP_HVAC.toString()))
                                     .build();
        asserter(context, true, expected, TransducerService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_one_transducer_by_device_not_associated_transitive_network(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"code\":\"NOT_FOUND\",\"message\":\"Not found resource with network_id=" + PrimaryKey.BACNET_NETWORK +
            " and device_id" + "=" + PrimaryKey.DEVICE_DROPLET + "\"}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE_DROPLET.toString())
                                                           .put("network_id", PrimaryKey.BACNET_NETWORK.toString())
                                                           .put("transducer_id",
                                                                PrimaryKey.TRANSDUCER_TEMP_HVAC.toString()))
                                     .build();
        asserter(context, false, expected, TransducerService.class.getName(), EventAction.GET_ONE, req);
    }

}
