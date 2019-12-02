package com.nubeiot.edge.module.datapoint.task.remote;

import java.util.UUID;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.edge.module.datapoint.MockData.ProtocolDispatcherAddress;
import com.nubeiot.edge.module.datapoint.service.DeviceService;
import com.nubeiot.edge.module.datapoint.service.NetworkService;
import com.nubeiot.edge.module.datapoint.task.remote.mock.MockProtocolNetworkHandler;
import com.nubeiot.iotdata.dto.DeviceType;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

public class ProtocolDispatcherTaskTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Protocol_Dispatcher();
    }

    @Test
    public void test_no_protocol_dispatcher(TestContext context) {
        final JsonObject resource = new JsonObject(
            "{\"id\":\"" + PrimaryKey.NETWORK + "\",\"code\":\"test\",\"edge\":\"" + PrimaryKey.EDGE + "\"," +
            "\"protocol\":\"BACNET\",\"state\":\"ENABLED\",\"metadata\":{\"host\":\"172.1.1.1\"}}");
        final JsonObject expected = new JsonObject().put("action", EventAction.UPDATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", resource);
        final Network network = new Network().setProtocol(Protocol.BACNET).setState(State.ENABLED).setCode("test");
        final RequestData req = RequestData.builder()
                                           .body(JsonPojo.from(network)
                                                         .toJson()
                                                         .put("network_id", UUID64.uuidToBase64(PrimaryKey.NETWORK))
                                                         .put("metadata", new JsonObject().put("host", "172.1.1.1")))
                                           .build();
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.UPDATE, req);
    }

    @Test
    public void test_protocol_dispatcher_not_enabled(TestContext context) {
        final UUID id = UUID.randomUUID();
        final JsonObject resource = new JsonObject(
            "{\"id\":\"" + id + "\",\"code\":\"test\",\"type\":\"GATEWAY\",\"protocol\":\"BACNET\"," +
            "\"state\":\"ENABLED\",\"name\":null,\"manufacturer\":null,\"model\":null,\"firmware_version\":null," +
            "\"software_version\":null,\"label\":null,\"metadata\":null}");
        final JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", resource);
        final Device device = new Device().setId(id)
                                          .setType(DeviceType.GATEWAY)
                                          .setProtocol(Protocol.BACNET)
                                          .setState(State.ENABLED)
                                          .setCode("test");
        final RequestData req = RequestData.builder().body(JsonPojo.from(device).toJson()).build();
        asserter(context, true, expected, DeviceService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_protocol_dispatcher_enable_and_data_is_updated(TestContext context) {
        final JsonObject metadata = new JsonObject().put("host", "192.168.1.10");
        final Network reqBody = new Network().setId(UUID.randomUUID())
                                             .setProtocol(Protocol.BACNET)
                                             .setState(State.ENABLED)
                                             .setCode("test");
        controller().register(ProtocolDispatcherAddress.NETWORK, new MockProtocolNetworkHandler(metadata));
        createNetwork(context, metadata, reqBody);
    }

    @Test
    public void test_protocol_dispatcher_enable_but_unreachable(TestContext context) {
        final JsonObject resource = new JsonObject().put("code", ErrorCode.SERVICE_NOT_FOUND)
                                                    .put("message",
                                                         "Protocol service is out of service. Try again later | " +
                                                         "Cause: Service unavailable - Error Code: SERVICE_ERROR | " +
                                                         "Cause: Service unavailable - Error Code: SERVICE_ERROR");
        // TODO recheck response of EventListener when no handler for address
        //        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
        //                                              .put("status", Status.FAILED)
        //                                              .put("error", resource);
        Network network = new Network().setProtocol(Protocol.BACNET).setState(State.ENABLED).setCode("test");
        asserter(context, false, resource, NetworkService.class.getName(), EventAction.CREATE,
                 RequestData.builder().body(JsonPojo.from(network).toJson()).build());
    }

    @Test
    public void test_protocol_dispatcher_enable_but_throw_error(TestContext context) {
        final Network pojo = new Network().setId(UUID.randomUUID())
                                          .setProtocol(Protocol.BACNET)
                                          .setState(State.ENABLED)
                                          .setCode("test");
        controller().register(ProtocolDispatcherAddress.NETWORK, new MockProtocolNetworkHandler(null));
        createNetwork(context, null, pojo);
        TestHelper.sleep(1000);
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("network_id", UUID64.uuidToBase64(pojo.getId())))
                                           .build();
        asserter(context, false,
                 new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT).put("message", "Unable to deleted"),
                 NetworkService.class.getName(), EventAction.REMOVE, req);
        TestHelper.sleep(1000);
        asserter(context, true, JsonPojo.from(pojo).toJson().put("edge", PrimaryKey.EDGE.toString()),
                 NetworkService.class.getName(), EventAction.GET_ONE, req);
    }

    private void createNetwork(TestContext context, JsonObject metadata, Network reqBody) {
        final JsonObject expectedBody = reqBody.toJson()
                                               .put("edge", PrimaryKey.EDGE.toString())
                                               .put("metadata", metadata);
        expectedBody.remove("sync_audit");
        expectedBody.remove("time_audit");
        final JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", expectedBody);
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.CREATE,
                 RequestData.builder().body(JsonPojo.from(reqBody).toJson()).build());
    }

}
