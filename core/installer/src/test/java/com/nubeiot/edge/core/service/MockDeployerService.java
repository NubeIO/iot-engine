package com.nubeiot.edge.core.service;

import java.util.function.Function;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.PreDeploymentResult;

public class MockDeployerService extends DeployerService {

    private final DeploymentAsserter asserter;

    public MockDeployerService(Vertx vertx, Function<String, Object> sharedDataFunc, EventModel postEvent,
                               DeploymentAsserter asserter) {
        super(vertx, sharedDataFunc, postEvent);
        this.asserter = asserter;
    }

    @Override
    void doDeploy(PreDeploymentResult preResult, Future<Object> future) {
        asserter.accept(preResult);
        future.complete();
    }

}
