package com.nubeiot.edge.module.datapoint.task.remote;

import java.util.UUID;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.edge.module.datapoint.MockData.ProtocolDispatcherAddress;
import com.nubeiot.edge.module.datapoint.service.NetworkService;
import com.nubeiot.edge.module.datapoint.task.remote.mock.MockProtocolNetworkHandler;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

public class ProtocolDispatcherTaskTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Protocol_Dispatcher();
    }

    @Test
    public void test_create_network_then_pass_to_bacnet(TestContext context) {
        controller().register(ProtocolDispatcherAddress.NETWORK,
                              new MockProtocolNetworkHandler(new JsonObject().put("host", "192.168.1.10")));
        final UUID id = UUID.randomUUID();
        final JsonObject resource = new JsonObject(
            "{\"id\":\"" + id + "\",\"code\":\"test\",\"edge\":\"" + PrimaryKey.EDGE + "\"," +
            "\"protocol\":\"BACNET\",\"state\":\"ENABLED\",\"label\":null,\"metadata\":{\"host\":\"192.168.1.10\"}}");
        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", resource);
        Network network = new Network().setId(id).setProtocol(Protocol.BACNET).setState(State.ENABLED).setCode("test");
        RequestData req = RequestData.builder().body(JsonPojo.from(network).toJson()).build();
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.CREATE, req);
    }

}
