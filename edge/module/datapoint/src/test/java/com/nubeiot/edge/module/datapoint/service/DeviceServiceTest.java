package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestFilter.Filters;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class DeviceServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Device_Equip_Thing();
    }

    @Test
    public void test_get_list_device(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"devices\":[{\"id\":\"" + PrimaryKey.DEVICE_DROPLET + "\",\"code\":\"DROPLET_01\"," +
            "\"type\":\"DROPLET\",\"protocol\":\"WIRE\",\"state\":\"NONE\",\"manufacturer\":\"NubeIO\"}," +
            "{\"id\":\"" + PrimaryKey.DEVICE_HVAC + "\",\"code\":\"HVAC_XYZ\",\"type\":\"HVAC\"," +
            "\"protocol\":\"BACNET\",\"state\":\"NONE\",\"manufacturer\":\"Lennox\"}]}");
        asserter(context, true, expected, DeviceService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build());
    }

    @Test
    public void test_delete_device_without_force(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.BEING_USED)
                                                    .put("message",
                                                         "Resource with device_id=" + PrimaryKey.DEVICE_DROPLET +
                                                         " is using by another resource");
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("device_id",
                                                                      UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET)))
                                           .build();
        asserter(context, false, expected, DeviceService.class.getName(), EventAction.REMOVE, req);
    }

    @Test
    public void test_delete_device_with_force(TestContext context) {
        final JsonObject body = new JsonObject(
            "{\"id\":\"" + PrimaryKey.DEVICE_DROPLET + "\",\"code\":\"DROPLET_01\"," +
            "\"type\":\"DROPLET\",\"protocol\":\"WIRE\",\"state\":\"NONE\",\"name\":null,\"manufacturer\":\"NubeIO\"," +
            "\"model\":null,\"firmware_version\":null,\"software_version\":null,\"label\":null,\"metadata\":null}");
        final JsonObject expected = new JsonObject().put("action", EventAction.REMOVE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", body);
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("device_id",
                                                                      UUID64.uuidToBase64(PrimaryKey.DEVICE_DROPLET)))
                                           .filter(new JsonObject().put(Filters.FORCE, true))
                                           .build();
        asserter(context, true, expected, DeviceService.class.getName(), EventAction.REMOVE, req);
    }

}
