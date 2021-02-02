package com.nubeiot.edge.connector.bacnet.service.discovery;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import io.github.zero88.qwe.EventbusHelper;
import io.github.zero88.qwe.JsonHelper;
import io.github.zero88.qwe.TestHelper;
import io.github.zero88.qwe.component.ReadinessAsserter;
import io.github.zero88.qwe.component.SharedLocalDataHelper;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.event.Status;
import io.github.zero88.qwe.exceptions.ErrorCode;
import io.github.zero88.qwe.iot.data.enums.DeviceStatus;
import io.github.zero88.qwe.micro.http.EventHttpService;
import io.github.zero88.qwe.protocol.Protocol;
import io.github.zero88.qwe.protocol.network.Ipv4Network;
import io.github.zero88.qwe.protocol.network.UdpProtocol;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.edge.connector.bacnet.BACnetApplication;
import com.nubeiot.edge.connector.bacnet.BACnetWithGatewayTest;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.service.mock.MockNetworkPersistService;
import com.nubeiot.edge.connector.bacnet.service.mock.MockProtocolDispatcherService;

public class BACnetNetworkExplorerPersistenceTest extends BACnetWithGatewayTest {

    MockNetworkPersistService networkService;

    protected ReadinessAsserter createReadinessHandler(TestContext context, Async async) {
        return new ReadinessAsserter(context, async, new JsonObject("{\"total\":0}"));
    }

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
        eventbus.request(BACnetNetworkExplorer.class.getName(),
                         EventMessage.initial(EventAction.CREATE, RequestData.builder().body(reqBody).build()),
                         EventbusHelper.replyAsserter(context, async, expected.toJson()));
    }

    @Test
    public void test_persist_network_success(TestContext context) throws IOException {
        final Async async = context.async(2);
        final UdpProtocol protocol = UdpProtocol.builder()
                                                .ip(Ipv4Network.getFirstActiveIp())
                                                .port(TestHelper.getRandomPort())
                                                .canReusePort(true)
                                                .build();
        final JsonObject reqBody = new JsonObject().put("networkCode", protocol.identifier());
        final JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource",
                                                         new JsonObject().put("protocol", Protocol.BACnet.type())
                                                                         .put("state", DeviceStatus.UP)
                                                                         .put("code", protocol.identifier())
                                                                         .put("metadata", protocol.toJson())
                                                                         .put("id", "exclude"));
        eventbus.request(BACnetNetworkExplorer.class.getName(),
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
    public void test_persist_network_already_existed(TestContext context) throws IOException {
        final UdpProtocol protocol = UdpProtocol.builder()
                                                .ip(Ipv4Network.getFirstActiveIp())
                                                .port(TestHelper.getRandomPort())
                                                .canReusePort(true)
                                                .build();
        final UUID networkId = UUID.randomUUID();
        getCache().addDataKey(protocol, networkId.toString());
        final Async async = context.async();
        final JsonObject reqBody = new JsonObject().put("networkCode", protocol.identifier());
        final EventMessage expected = EventMessage.error(EventAction.CREATE, ErrorCode.ALREADY_EXIST,
                                                         "Already persisted network code " + protocol.identifier() +
                                                         " with id " + networkId);
        eventbus.request(BACnetNetworkExplorer.class.getName(),
                         EventMessage.initial(EventAction.CREATE, RequestData.builder().body(reqBody).build()),
                         EventbusHelper.replyAsserter(context, async, expected.toJson()));
    }

    @Test
    public void test_persist_network_failed(TestContext context) throws IOException {
        networkService.errorInCreate(true);
        final Async async = context.async(2);
        final UdpProtocol protocol = UdpProtocol.builder()
                                                .ip(Ipv4Network.getFirstActiveIp())
                                                .port(TestHelper.getRandomPort())
                                                .canReusePort(true)
                                                .build();
        final JsonObject reqBody = new JsonObject().put("networkCode", protocol.identifier());
        final JsonObject expected = new JsonObject().put("code", ErrorCode.UNKNOWN_ERROR).put("message", "Failed");
        eventbus.request(BACnetNetworkExplorer.class.getName(),
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
        return SharedLocalDataHelper.getLocalDataValue(vertx, BACnetApplication.class.getName(),
                                                       BACnetCacheInitializer.LOCAL_NETWORK_CACHE);
    }

}
