package com.nubeiot.edge.connector.bacnet.service.discover;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetWithGatewayTest;
import com.nubeiot.iotdata.dto.Protocol;

public class NetworkPersistenceTest extends BACnetWithGatewayTest {

    @Test
    public void test_mock_persist_network(TestContext context) {
        final Async async = context.async();
        final UdpProtocol protocol = UdpProtocol.builder()
                                                .ip(Ipv4Network.getFirstActiveIp())
                                                .port(47808)
                                                .canReusePort(true)
                                                .build();
        final JsonObject reqBody = new JsonObject().put("networkCode", protocol.identifier());
        final JsonObject expected = new JsonObject().put("protocol", Protocol.BACNET.type())
                                                    .put("state", State.ENABLED)
                                                    .put("code", protocol.identifier())
                                                    .put("metadata", protocol.toJson())
                                                    .put("id", "exclude");
        busClient.request(NetworkDiscovery.class.getName(),
                          EventMessage.initial(EventAction.CREATE, RequestData.builder().body(reqBody).build()),
                          EventbusHelper.replyAsserter(context, async,
                                                       EventMessage.success(EventAction.CREATE, expected).toJson(),
                                                       JsonHelper.ignore("data.id")));
    }

}
