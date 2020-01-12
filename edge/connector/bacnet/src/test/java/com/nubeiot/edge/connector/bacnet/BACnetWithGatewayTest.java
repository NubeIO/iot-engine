package com.nubeiot.edge.connector.bacnet;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.ReadinessAsserter;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroConfig;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.Microservice;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.edge.connector.bacnet.service.mock.MockNetworkPersistService;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointApiService;

import lombok.NonNull;

public abstract class BACnetWithGatewayTest extends BaseBACnetVerticleTest {

    protected final String apiName = DataPointApiService.DEFAULT.lookupApiName(NetworkMetadata.INSTANCE);
    protected final String address = MockNetworkPersistService.class.getName();

    protected MicroConfig getMicroConfig() {
        return IConfig.fromClasspath("mockGateway.json", MicroConfig.class);
    }

    protected void deployMicroservice(Vertx vertx, TestContext context, Async async) {
        final Microservice verticle = new MicroserviceProvider().get();
        VertxHelper.deploy(vertx, context, createDeploymentOptions(getMicroConfig()), verticle, s -> {
            registerMockGatewayService(context, async, verticle.getContext());
            TestHelper.testComplete(async);
        });
    }

    protected ReadinessAsserter createReadinessHandler(TestContext context, Async async) {
        return new ReadinessAsserter(context, async, new JsonObject("{\"total\":1}"));
    }

    protected void deployServices(TestContext context, BACnetConfig bacnetCfg, BACnetVerticle verticle) {
        final Async async = context.async(4);
        VertxHelper.deploy(vertx, context, createDeploymentOptions(bacnetCfg), verticle, event -> {
            deployMicroservice(vertx, context, async);
            busClient = verticle.getEventbusClient()
                                .register(bacnetCfg.getReadinessAddress(), createReadinessHandler(context, async));
            TestHelper.testComplete(async);
        });
    }

    protected MockNetworkPersistService createMockRemoteNetworkService() {
        return MockNetworkPersistService.builder().hasNetworks(true).build();
    }

    private void registerMockGatewayService(@NonNull TestContext context, @NonNull Async async,
                                            @NonNull MicroContext microContext) {
        final MockNetworkPersistService listener = createMockRemoteNetworkService();
        final ActionMethodMapping mapping = ActionMethodMapping.byCRUD(listener.getAvailableEvents());
        final EventMethodDefinition definition = EventMethodDefinition.create("/api/test", mapping);
        microContext.getLocalController()
                    .addEventMessageRecord(apiName, address, definition)
                    .doOnSuccess(r -> busClient.register(address, listener))
                    .subscribe(r -> TestHelper.testComplete(async), context::fail);
    }

}
