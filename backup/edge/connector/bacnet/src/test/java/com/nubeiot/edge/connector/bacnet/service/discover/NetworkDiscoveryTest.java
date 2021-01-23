package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Optional;

import org.junit.Test;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.NetworkException;
import com.nubeiot.core.protocol.network.IpNetwork;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetWithoutGatewayTest;
import com.nubeiot.edge.connector.bacnet.dto.BACnetIP;

public class NetworkDiscoveryTest extends BACnetWithoutGatewayTest {

    @Test
    public void test_get_networks(TestContext context) {
        final Async async = context.async();
        Handler<JsonObject> handler = json -> {
            final EventMessage eventMessage = EventMessage.tryParse(json);
            try {
                context.assertEquals(Status.SUCCESS, eventMessage.getStatus());
                context.assertEquals(EventAction.GET_LIST, eventMessage.getAction());
                JsonObject ip = Optional.ofNullable(eventMessage.getData()).map(js -> {
                    System.out.println(js.encodePrettily());
                    return js.getJsonObject("ipv4", new JsonObject());
                }).orElseGet(JsonObject::new);
                context.assertNotEquals(0, ip.size());
            } finally {
                TestHelper.testComplete(async);
            }
        };
        busClient.request(NetworkDiscovery.class.getName(),
                          EventMessage.initial(EventAction.GET_LIST, new JsonObject()),
                          EventbusHelper.replyAsserter(context, handler));
    }

    @Test
    public void test_get_missing_network_code(TestContext context) {
        final Async async = context.async();
        final BACnetIP dockerIp = BACnetIP.builder().subnet("192.168.16.1/20").label("docker").build();
        final JsonObject body = new JsonObject().put("network", dockerIp.toJson());
        final JsonObject expected = new JsonObject(
            "{\"status\":\"FAILED\",\"action\":\"GET_ONE\",\"error\":{\"code\":\"INVALID_ARGUMENT\"," +
            "\"message\":\"Missing BACnet network code\"}}");
        busClient.request(NetworkDiscovery.class.getName(),
                          EventMessage.initial(EventAction.GET_ONE, RequestData.builder().body(body).build()),
                          EventbusHelper.replyAsserter(context, async, expected));
    }

    @Test
    public void test_get_available_network(TestContext context) {
        final IpNetwork network = Ipv4Network.getActiveIps()
                                             .stream()
                                             .findFirst()
                                             .orElseThrow(() -> new NetworkException("Failed"));
        final Async async = context.async();
        final JsonObject request = new JsonObject().put("networkCode", network.identifier());
        final JsonObject response = new JsonObject().put("network", UdpProtocol.builder()
                                                                               .ip(network)
                                                                               .port(47808)
                                                                               .canReusePort(true)
                                                                               .build()
                                                                               .toJson());
        final JsonObject expected = EventMessage.success(EventAction.GET_ONE, response).toJson();
        busClient.request(NetworkDiscovery.class.getName(),
                          EventMessage.initial(EventAction.GET_ONE, RequestData.builder().body(request).build()),
                          EventbusHelper.replyAsserter(context, async, expected));
    }

}
