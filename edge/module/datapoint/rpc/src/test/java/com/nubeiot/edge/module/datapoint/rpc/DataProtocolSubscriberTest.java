package com.nubeiot.edge.module.datapoint.rpc;

import java.util.UUID;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.micro.discovery.RemoteServiceInvoker;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.edge.module.datapoint.MockData.ProtocolDispatcherAddress;
import com.nubeiot.edge.module.datapoint.rpc.mock.MockNetworkSubscriber;
import com.nubeiot.edge.module.datapoint.service.NetworkService;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

public class DataProtocolSubscriberTest extends BaseDataPointServiceTest {

    private JsonObject metadata;

    @Override
    protected void setup(TestContext context) {
        super.setup(context);
        this.metadata = new JsonObject().put("host", "123.456.789.1");
        controller().register(ProtocolDispatcherAddress.NETWORK, new MockNetworkSubscriber(vertx, sharedKey, metadata));
    }

    @Override
    protected JsonObject testData() {
        return MockData.data_Protocol_Dispatcher();
    }

    @Test
    public void test_dispatch_to_subscriber(TestContext context) {
        final Network network = new Network().setId(UUID.randomUUID())
                                             .setState(State.ENABLED)
                                             .setProtocol(Protocol.BACNET)
                                             .setCode("xxx");
        final JsonObject resource = network.toJson().put("edge", PrimaryKey.EDGE.toString()).put("metadata", metadata);
        resource.remove("sync_audit");
        resource.remove("time_audit");
        final JsonObject expected = new JsonObject().put("status", Status.SUCCESS)
                                                    .put("action", EventAction.CREATE)
                                                    .put("resource", resource);
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.CREATE,
                 RequestData.builder().body(JsonPojo.from(network).toJson()).build());
    }

    @Test
    public void test_unable_dispatch_due_to_round_robin(TestContext context) {
        final Network network = new Network().setId(UUID.randomUUID())
                                             .setState(State.ENABLED)
                                             .setProtocol(Protocol.BACNET)
                                             .setCode("xxx");
        final JsonObject resource = network.toJson().put("edge", PrimaryKey.EDGE.toString());
        resource.remove("sync_audit");
        resource.remove("time_audit");
        final JsonObject expected = new JsonObject().put("status", Status.SUCCESS)
                                                    .put("action", EventAction.CREATE)
                                                    .put("resource", resource);
        final JsonObject headers = new JsonObject().put(Headers.X_REQUEST_BY,
                                                        RemoteServiceInvoker.requestBy(Protocol.BACNET.type()));
        final RequestData reqData = RequestData.builder().body(JsonPojo.from(network).toJson())
                                               .headers(headers)
                                               .build();
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.CREATE, reqData);
    }

}
