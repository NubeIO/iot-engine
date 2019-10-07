package com.nubeiot.edge.core.service;

import java.util.function.Consumer;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.edge.core.model.dto.PreDeploymentResult;

public interface DeploymentAsserter extends Consumer<PreDeploymentResult> {

    static DeploymentAsserter init(io.vertx.reactivex.core.Vertx vertx, TestContext context, String moduleServiceAddr,
                                   String transServiceAddr) {
        return init(vertx.getDelegate(), context, moduleServiceAddr, transServiceAddr);
    }

    static DeploymentAsserter init(Vertx vertx, TestContext context, String moduleServiceAddr,
                                   String transServiceAddr) {
        return preDeploymentResult -> {
            JsonObject serviceBody = new JsonObject().put("service_id", preDeploymentResult.getServiceId());
            EventMessage serviceMessage = EventMessage.success(EventAction.GET_ONE,
                                                               RequestData.builder().body(serviceBody).build());

            JsonObject transactionBody = new JsonObject().put("transaction_id", preDeploymentResult.getTransactionId());
            EventMessage transactionMessage = EventMessage.success(EventAction.GET_ONE,
                                                                   RequestData.builder().body(transactionBody).build());
            final Async async = context.async(2);

            vertx.eventBus().send(moduleServiceAddr, serviceMessage.toJson(), result -> {
                System.out.println("Asserting module");
                JsonObject body = (JsonObject) result.result().body();
                context.assertEquals(body.getString("status"), Status.SUCCESS.name());
                JsonObject data = body.getJsonObject("data");
                context.assertEquals(data.getString("state"), State.PENDING.name());
                TestHelper.testComplete(async);
            });

            vertx.eventBus().send(transServiceAddr, transactionMessage.toJson(), result -> {
                System.out.println("Asserting transaction");
                JsonObject body = (JsonObject) result.result().body();
                context.assertEquals(body.getString("status"), Status.SUCCESS.name());
                JsonObject data = body.getJsonObject("data");
                context.assertEquals(data.getString("status"), Status.WIP.name());
                TestHelper.testComplete(async);
            });
        };
    }

}
