package com.nubeiot.edge.connector.bacnet.service.discover;

import org.junit.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetWithoutGatewayTest;

public class DeviceDiscoveryTest extends BACnetWithoutGatewayTest {

    @Test
    public void test_network_without_device(TestContext context) {
        final Async async = context.async();
        final UdpProtocol protocol = UdpProtocol.builder().ip(Ipv4Network.getFirstActiveIp()).port(47808).build();
        final JsonObject body = new JsonObject().put("networkCode", protocol.identifier());
        final JsonObject expected = EventMessage.success(EventAction.GET_LIST,
                                                         new JsonObject().put("remoteDevices", new JsonArray()))
                                                .toJson();
        busClient.request(DeviceDiscovery.class.getName(),
                          EventMessage.initial(EventAction.GET_LIST, RequestData.builder().body(body).build()),
                          EventbusHelper.replyAsserter(context, async, expected));
    }

}
