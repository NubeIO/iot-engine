package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.component.ReadinessAsserter;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;
import com.nubeiot.edge.connector.bacnet.BACnetWithGatewayTest;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.service.mock.MockNetworkPersistService;
import com.nubeiot.edge.connector.bacnet.service.mock.MockProtocolDispatcherService;
import com.nubeiot.iotdata.dto.Protocol;

public class NetworkDiscoveryPersistenceTest extends BACnetWithGatewayTest {

    MockNetworkPersistService networkService;

    protected ReadinessAsserter createReadinessHandler(TestContext context, Async async) {
        return new ReadinessAsserter(context, async, new JsonObject("{\"total\":0}"));
    }

    @Override
    protected Set<EventHttpService> serviceDefinitions() {
        networkService = MockNetworkPersistService.builder().hasNetworks(false).build();
        return new HashSet<>(Arrays.asList(networkService, new MockProtocolDispatcherService()));
    }

    @Test
    public void test_persist_network_invalid(TestContext context) {
        final Async async = context.async();
        final JsonObject reqBody = new JsonObject().put("networkCode", "xyz");
        final EventMessage expected = EventMessage.error(EventAction.CREATE, ErrorCode.NOT_FOUND,
                                                         "Not found active IP network interface with name xyz");
        busClient.request(NetworkDiscovery.class.getName(),
                          EventMessage.initial(EventAction.CREATE, RequestData.builder().body(reqBody).build()),
                          EventbusHelper.replyAsserter(context, async, expected.toJson()));
    }

    @Test
    public void test_persist_network_success(TestContext context) {
        final Async async = context.async(2);
        final UdpProtocol protocol = UdpProtocol.builder()
                                                .ip(Ipv4Network.getFirstActiveIp())
                                                .port(47808)
                                                .canReusePort(true)
                                                .build();
        final JsonObject reqBody = new JsonObject().put("networkCode", protocol.identifier());
        final JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource",
                                                         new JsonObject().put("protocol", Protocol.BACNET.type())
                                                                         .put("state", State.ENABLED)
                                                                         .put("code", protocol.identifier())
                                                                         .put("metadata", protocol.toJson())
                                                                         .put("id", "exclude"));
        busClient.request(NetworkDiscovery.class.getName(),
                          EventMessage.initial(EventAction.CREATE, RequestData.builder().body(reqBody).build()))
                 .filter(EventMessage::isSuccess)
                 .switchIfEmpty(Single.error(new RuntimeException("Failed in setup mock service persist")))
                 .map(EventMessage::getData)
                 .map(data -> {
                     JsonHelper.assertJson(context, async, expected, data, JsonHelper.ignore("resource.id"));
                     return data.getJsonObject("resource").getString("id");
                 })
                 .map(id -> {
                     final UUID key = getCache().getDataKey(protocol.identifier()).orElse(null);
                     Assert.assertNotNull(key);
                     context.assertEquals(id, key.toString());
                     return id;
                 })
                 .subscribe(t -> TestHelper.testComplete(async), context::fail);
    }

    @Test
    public void test_persist_network_already_existed(TestContext context) {
        final UdpProtocol protocol = UdpProtocol.builder()
                                                .ip(Ipv4Network.getFirstActiveIp())
                                                .port(47808)
                                                .canReusePort(true)
                                                .build();
        final UUID networkId = UUID.randomUUID();
        getCache().addDataKey(protocol, networkId.toString());
        final Async async = context.async();
        final JsonObject reqBody = new JsonObject().put("networkCode", protocol.identifier());
        final EventMessage expected = EventMessage.error(EventAction.CREATE, ErrorCode.ALREADY_EXIST,
                                                         "Already persisted network code " + protocol.identifier() +
                                                         " with id " + networkId);
        busClient.request(NetworkDiscovery.class.getName(),
                          EventMessage.initial(EventAction.CREATE, RequestData.builder().body(reqBody).build()),
                          EventbusHelper.replyAsserter(context, async, expected.toJson()));
    }

    @Test
    public void test_persist_network_failed(TestContext context) {
        networkService.errorInCreate(true);
        final Async async = context.async(2);
        final UdpProtocol protocol = UdpProtocol.builder()
                                                .ip(Ipv4Network.getFirstActiveIp())
                                                .port(47808)
                                                .canReusePort(true)
                                                .build();
        final JsonObject reqBody = new JsonObject().put("networkCode", protocol.identifier());
        final JsonObject expected = new JsonObject().put("code", ErrorCode.UNKNOWN_ERROR).put("message", "Failed");
        busClient.request(NetworkDiscovery.class.getName(),
                          EventMessage.initial(EventAction.CREATE, RequestData.builder().body(reqBody).build()))
                 .filter(EventMessage::isError)
                 .switchIfEmpty(Single.error(new RuntimeException("Failed in setup mock service persist")))
                 .map(EventMessage::getError)
                 .map(error -> {
                     context.assertFalse(getCache().getDataKey(protocol.identifier()).isPresent());
                     return error;
                 })
                 .doOnSuccess(error -> JsonHelper.assertJson(context, async, expected, error.toJson()))
                 .subscribe(t -> TestHelper.testComplete(async), context::fail);
    }

    private BACnetNetworkCache getCache() {
        return SharedDataDelegate.getLocalDataValue(vertx, BACnetVerticle.class.getName(),
                                                    BACnetCacheInitializer.EDGE_NETWORK_CACHE);
    }

}
