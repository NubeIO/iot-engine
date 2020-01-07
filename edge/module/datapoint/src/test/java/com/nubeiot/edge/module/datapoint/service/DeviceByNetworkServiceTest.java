package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.DeviceType;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

public class DeviceByNetworkServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Device_Equip_Thing();
    }

    @Test
    public void test_get_list_device_by_edge(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"devices\":[{\"id\":2,\"device\":{\"id\":\"" + PrimaryKey.DEVICE_HVAC + "\"," +
            "\"code\":\"HVAC_XYZ\",\"type\":\"HVAC\",\"protocol\":\"BACNET\",\"state\":\"NONE\"," +
            "\"manufacturer\":\"Lennox\"}}]}");
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("edge_id", UUID64.uuidToBase64(PrimaryKey.EDGE))
                                                                 .put("network_id",
                                                                      UUID64.uuidToBase64(PrimaryKey.BACNET_NETWORK)))
                                           .build();
        asserter(context, true, expected, DeviceByNetworkService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_create_device_by_edge_implicit_network(TestContext context) {
        final Device device = new Device().setId(UUID.randomUUID()).setType(DeviceType.MACHINE).setCode("MACHINE_01");
        final JsonObject address = new JsonObject().put("pin", "01");
        final JsonObject reqBody = new JsonObject().put("device", JsonPojo.from(new Device(device)).toJson())
                                                   .put("address", address)
                                                   .put("edge_id", UUID64.uuidToBase64(PrimaryKey.EDGE));
        createThenAssert(context, device.setProtocol(Protocol.UNKNOWN).setState(State.NONE), address, reqBody);
    }

    @Test
    public void test_create_device_by_edge_n_explicit_network(TestContext context) {
        final Device device = new Device().setId(UUID.randomUUID())
                                          .setType(DeviceType.DROPLET)
                                          .setCode("DROPLET_02")
                                          .setProtocol(Protocol.BACNET)
                                          .setState(State.ENABLED)
                                          .setManufacturer("NubeIO");
        final JsonObject address = new JsonObject().put("hostAddress", "xxx");
        final JsonObject reqBody = new JsonObject().put("device", JsonPojo.from(device).toJson())
                                                   .put("address", address)
                                                   .put("edge_id", UUID64.uuidToBase64(PrimaryKey.EDGE))
                                                   .put("network_id", UUID64.uuidToBase64(PrimaryKey.BACNET_NETWORK));
        createThenAssert(context, device, address, reqBody);
    }

    private void createThenAssert(TestContext context, Device response, JsonObject address, JsonObject reqBody) {
        final JsonObject resource = new JsonObject().put("id", 3)
                                                    .put("address", address)
                                                    .put("device", JsonPojo.from(response)
                                                                           .toJson(JsonData.MAPPER,
                                                                                   EntityTransformer.AUDIT_FIELDS));
        final JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", resource);
        final RequestData req = RequestData.builder().body(reqBody).build();
        asserter(context, true, expected, DeviceByNetworkService.class.getName(), EventAction.CREATE, req);
    }

}
