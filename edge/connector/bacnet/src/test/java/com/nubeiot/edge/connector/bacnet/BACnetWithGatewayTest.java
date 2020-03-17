package com.nubeiot.edge.connector.bacnet;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.component.EventClientProxy;
import com.nubeiot.core.component.ReadinessAsserter;
import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.micro.MicroConfig;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.Microservice;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.register.EventHttpServiceRegister;

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
