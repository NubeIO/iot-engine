package com.nubeiot.edge.connector.bacnet;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import io.github.zero88.qwe.IConfig;
import io.github.zero88.qwe.TestHelper;
import io.github.zero88.qwe.TestHelper.VertxHelper;
import io.github.zero88.qwe.component.ContainerVerticle;
import io.github.zero88.qwe.component.EventClientProxy;
import io.github.zero88.qwe.component.ReadinessAsserter;
import io.github.zero88.qwe.micro.MicroConfig;
import io.github.zero88.qwe.micro.MicroContext;
import io.github.zero88.qwe.micro.Microservice;
import io.github.zero88.qwe.micro.MicroserviceProvider;
import io.github.zero88.qwe.micro.metadata.EventHttpService;
import io.github.zero88.qwe.micro.register.EventHttpServiceRegister;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.servicediscovery.Record;

import lombok.NonNull;

public abstract class BACnetWithGatewayTest extends BaseBACnetVerticleTest {

    protected ReadinessAsserter createReadinessHandler(TestContext context, Async async) {
        return new ReadinessAsserter(context, async, new JsonObject("{\"total\":1}"));
    }

    protected void deployServices(TestContext context) {
        busClient = EventClientProxy.create(vertx, null).transporter();
        final Async async = context.async(2);
        deployVerticle(vertx, context, async, () -> deployBACnetVerticle(context, async));
    }

    protected MicroConfig getMicroConfig() {
        return IConfig.fromClasspath("mockGateway.json", MicroConfig.class);
    }

    protected void deployVerticle(Vertx vertx, TestContext context, Async async, Supplier<ContainerVerticle> supplier) {
        final Microservice v = new MicroserviceProvider().get();
        VertxHelper.deploy(vertx, context, createDeploymentOptions(getMicroConfig()), v, id -> {
            registerMockService(v.getContext()).map(i -> supplier.get())
                                               .subscribe(i -> TestHelper.testComplete(async), context::fail);
        });
    }

    protected abstract Set<EventHttpService> serviceDefinitions();

    private Single<List<Record>> registerMockService(@NonNull MicroContext microContext) {
        return EventHttpServiceRegister.builder()
                                       .vertx(vertx)
                                       .sharedKey(BACnetVerticle.class.getName())
                                       .eventServices(this::serviceDefinitions)
                                       .build()
                                       .publish(microContext.getLocalController());
    }

}
