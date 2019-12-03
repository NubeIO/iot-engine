package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroConfig;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetVerticleTest;
import com.nubeiot.edge.connector.bacnet.service.mock.NetworkPersistService;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.iotdata.dto.Protocol;

public class NetworkPersistenceTest extends BACnetVerticleTest {

    private final String apiName = DataPointIndex.lookupApiName(NetworkMetadata.INSTANCE);
    private final String address = NetworkPersistService.class.getName();

    @Override
    protected Optional<MicroConfig> getMicroConfig() {
        return Optional.of(IConfig.fromClasspath("mockGateway.json", MicroConfig.class));
    }

    @Override
    protected void registerMockGatewayService(TestContext context, Async async, MicroContext microContext) {
        final EventMethodDefinition definition = EventMethodDefinition.create("/api/test", ActionMethodMapping.byCRUD(
            Arrays.asList(EventAction.CREATE, EventAction.GET_LIST)));
        microContext.getLocalController()
                    .addEventMessageRecord(apiName, address, definition)
                    .subscribe(r -> TestHelper.testComplete(async), context::fail);
        busClient.register(address, new NetworkPersistService());
    }

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
                                                    .put("metadata", protocol.toJson());
        busClient.request(NetworkDiscovery.class.getName(),
                          EventMessage.initial(EventAction.CREATE, RequestData.builder().body(reqBody).build()),
                          EventbusHelper.replyAsserter(context, async,
                                                       EventMessage.success(EventAction.CREATE, expected).toJson()));
    }

}
