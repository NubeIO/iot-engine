package com.nubeiot.edge.connector.bacnet.simulator;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.ErrorData;
import com.nubeiot.edge.connector.bacnet.handler.DiscoverCompletionHandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SimulatorCompletionAsserter extends DiscoverCompletionHandler {

    @NonNull
    private final TestContext context;
    @NonNull
    private final Async async;
    @NonNull
    private final JsonObject expected;

    @Override
    public boolean success(@NonNull RequestData requestData) {
        JsonHelper.assertJson(context, async, expected, requestData.body());
        return true;
    }

    @Override
    public boolean error(@NonNull ErrorData error) {
        JsonHelper.assertJson(context, async, expected, error.toJson());
        return true;
    }

}
