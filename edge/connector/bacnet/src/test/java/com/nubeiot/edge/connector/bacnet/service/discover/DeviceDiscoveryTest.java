package com.nubeiot.edge.connector.bacnet.service.discover;

import org.junit.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetVerticleTest;

public class DeviceDiscoveryTest extends BACnetVerticleTest {

    @Test
    public void test_network_without_device(TestContext context) {
        final UdpProtocol protocol = UdpProtocol.builder().ip(Ipv4Network.getFirstActiveIp()).port(47808).build();
        Async async = context.async();
        final JsonObject body = new JsonObject().put("networkCode", protocol.identifier());
        final JsonObject expected = EventMessage.success(EventAction.GET_LIST,
                                                         new JsonObject().put("remoteDevices", new JsonArray()))
                                                .toJson();
        busClient.request(DeliveryEvent.builder()
                                       .address(DeviceDiscovery.class.getName())
                                       .action(EventAction.GET_LIST)
                                       .addPayload(RequestData.builder().body(body).build())
                                       .build(), EventbusHelper.replyAsserter(context, async, expected));
    }

}
