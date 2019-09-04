package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class DeviceEquipServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Equip_Thing();
    }

    @Test
    public void test_get_list_equip_by_device(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"equipments\":[{\"id\":1,\"equipment\":{\"id\":\"" + PrimaryKey.EQUIP_DROPLET +
            "\",\"code\":\"DROPLET_01\",\"type\":\"DROPLET\",\"manufacturer\":\"NubeIO\"}}," +
            "{\"id\":2,\"network\":\"01fbb11e-45a6-479b-91a4-003534770c1c\",\"equipment\":{\"id\":\"" +
            PrimaryKey.EQUIP_HVAC + "\",\"code\":\"HVAC_XYZ\",\"type\":\"HVAC\",\"manufacturer\":\"Lennox\"}}]}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE.toString()))
                                     .build();
        asserter(context, true, expected, EquipmentByDevice.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_create_equip_by_device_invalid_request(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                              .put("message", "Missing equipment data");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("code", "DROPLET_02")
                                                           .put("type", "DROPLET")
                                                           .put("manufacturer", "NubeIO")
                                                           .put("device_id", PrimaryKey.DEVICE.toString()))
                                     .build();
        asserter(context, false, expected, EquipmentByDevice.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_equip_by_device(TestContext context) {
        final String id = UUID.randomUUID().toString();
        final JsonObject data = new JsonObject(
            "{\"id\":3,\"network\":null,\"equipment\":{\"id\":\"" + id + "\",\"code\":\"DROPLET_02\"," +
            "\"label\":null,\"type\":\"DROPLET\",\"manufacturer\":\"NubeIO\",\"metadata\":null}}");
        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", data);
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("equipment", new JsonObject().put("id", id)
                                                                                             .put("code", "DROPLET_02")
                                                                                             .put("type", "DROPLET")
                                                                                             .put("manufacturer",
                                                                                                  "NubeIO"))
                                                           .put("device_id", PrimaryKey.DEVICE.toString()))
                                     .build();
        asserter(context, true, expected, EquipmentByDevice.class.getName(), EventAction.CREATE, req);
    }

}
