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

public class DeviceByNetworkServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Device_Equip_Thing();
    }

    @Test
    public void test_get_list_device_by_edge(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"devices\":[{\"id\":2,\"device\":{\"id\":\"" + PrimaryKey.DEVICE_HVAC + "\"," +
            "\"code\":\"HVAC_XYZ\",\"type\":\"HVAC\",\"protocol\":\"UNKNOWN\",\"state\":\"NONE\"," +
            "\"manufacturer\":\"Lennox\"}}]}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("network_id", PrimaryKey.NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, DeviceByNetworkService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_create_device_by_edge_invalid_request(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                              .put("message", "Missing key network_id");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("code", "DROPLET_02")
                                                           .put("type", "DROPLET")
                                                           .put("manufacturer", "NubeIO"))
                                     .build();
        asserter(context, false, expected, DeviceByNetworkService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_device_by_edge(TestContext context) {
        final String id = UUID.randomUUID().toString();
        final JsonObject resource = new JsonObject(
            "{\"id\":3,\"address\":{\"hostAddress\":\"xxx\"},\"device\":{\"id\":\"" + id + "\"," +
            "\"code\":\"DROPLET_02\",\"type\":\"DROPLET\",\"protocol\":\"UNKNOWN\",\"name\":null,\"state\":\"NONE\"," +
            "\"manufacturer\":\"NubeIO\",\"model\":null,\"firmware_version\":null,\"software_version\":null," +
            "\"label\":null,\"metadata\":null}}");
        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", resource);
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device", new JsonObject().put("id", id)
                                                                                          .put("code", "DROPLET_02")
                                                                                          .put("type", "DROPLET")
                                                                                          .put("manufacturer",
                                                                                               "NubeIO"))
                                                           .put("address", new JsonObject().put("hostAddress", "xxx"))
                                                           .put("network_id", PrimaryKey.NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, DeviceByNetworkService.class.getName(), EventAction.CREATE, req);
    }

}
