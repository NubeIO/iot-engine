package com.nubeiot.edge.module.datapoint.service;

import java.util.Objects;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.zero.utils.UUID64;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.DeviceType;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;

public class DeviceByNetworkServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Device_Transducer();
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
    public void test_assign_not_found_device_to_edge(TestContext context) {
        final UUID id = UUID.randomUUID();
        final JsonObject reqBody = new JsonObject().put("device_id", id.toString())
                                                   .put("edge_id", UUID64.uuidToBase64(PrimaryKey.EDGE))
                                                   .put("network_id", UUID64.uuidToBase64(PrimaryKey.BACNET_NETWORK));
        final RequestData req = RequestData.builder().body(reqBody).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message", "Not found resource with device_id=" + id);
        asserter(context, false, expected, DeviceByNetworkService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_assign_new_device_to_edge(TestContext context) {
        final Device device = new Device().setId(UUID.randomUUID()).setType(DeviceType.MACHINE).setCode("MACHINE_01");
        final RequestData req1 = RequestData.builder().body(JsonPojo.from(new Device(device)).toJson()).build();
        final JsonObject reqBody = new JsonObject().put("device_id", device.getId().toString())
                                                   .put("edge_id", UUID64.uuidToBase64(PrimaryKey.EDGE))
                                                   .put("network_id", UUID64.uuidToBase64(PrimaryKey.BACNET_NETWORK));
        final RequestData req = RequestData.builder().body(reqBody).build();
        final Async async = context.async();
        controller().request(DeviceService.class.getName(), EventMessage.initial(EventAction.CREATE, req1))
                    .filter(EventMessage::isSuccess)
                    .switchIfEmpty(Single.error(new RuntimeException("Cannot create device")))
                    .flatMap(ignore -> controller().request(DeviceByNetworkService.class.getName(),
                                                            EventMessage.initial(EventAction.CREATE, req)))
                    .map(msg -> {
                        context.assertTrue(msg.isSuccess());
                        JsonObject expected = createSuccessResponse(
                            device.setProtocol(Protocol.UNKNOWN).setState(State.NONE), null);
                        JsonHelper.assertJson(context, async, expected, Objects.requireNonNull(msg.getData()),
                                              JSONCompareMode.STRICT);
                        return msg;
                    })
                    .subscribe(s -> TestHelper.testComplete(async), context::fail);
    }

    @Test
    public void test_create_device_by_network_implicit_edge(TestContext context) {
        final Device device = new Device().setId(UUID.randomUUID()).setType(DeviceType.MACHINE).setCode("MACHINE_01");
        final JsonObject address = new JsonObject().put("pin", "01");
        final JsonObject reqBody = new JsonObject().put("device", JsonPojo.from(new Device(device)).toJson())
                                                   .put("address", address);
        createThenAssert(context, device.setProtocol(Protocol.UNKNOWN).setState(State.NONE), address, reqBody);
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

    @Test
    @Ignore
    //FIXME https://github.com/NubeIO/iot-engine/issues/311
    public void test_patch_device_to_another_network(TestContext context) {
        final JsonObject address = new JsonObject().put("hostAddress", "xxx");
        final JsonObject reqBody = new JsonObject().put("address", address)
                                                   .put("device_id", PrimaryKey.DEVICE_DROPLET.toString())
                                                   .put("edge_id", UUID64.uuidToBase64(PrimaryKey.EDGE))
                                                   .put("network", new JsonObject().put("id", UUID64.uuidToBase64(
                                                       PrimaryKey.BACNET_NETWORK)));
        final JsonObject device = JsonPojo.from(MockData.searchDevice(PrimaryKey.DEVICE_DROPLET).setState(State.NONE))
                                          .toJson();
        final JsonObject resource = new JsonObject().put("id", 1)
                                                    .put("address", address)
                                                    .put("network_id", PrimaryKey.BACNET_NETWORK.toString())
                                                    .put("device", device);
        asserter(context, true, new JsonObject().put("action", EventAction.PATCH)
                                                .put("status", Status.SUCCESS)
                                                .put("resource", resource), DeviceByNetworkService.class.getName(),
                 EventAction.PATCH, RequestData.builder().body(reqBody).build());
    }

    private void createThenAssert(TestContext context, Device response, JsonObject address, JsonObject reqBody) {
        final JsonObject expected = createSuccessResponse(response, address);
        final RequestData req = RequestData.builder().body(reqBody).build();
        asserter(context, true, expected, DeviceByNetworkService.class.getName(), EventAction.CREATE, req);
    }

    private JsonObject createSuccessResponse(Device response, JsonObject address) {
        final JsonObject resource = new JsonObject().put("id", 3)
                                                    .put("address", address)
                                                    .put("device", JsonPojo.from(response)
                                                                           .toJson(JsonData.MAPPER,
                                                                                   EntityTransformer.AUDIT_FIELDS));
        return new JsonObject().put("action", EventAction.CREATE)
                               .put("status", Status.SUCCESS)
                               .put("resource", resource);
    }

}
