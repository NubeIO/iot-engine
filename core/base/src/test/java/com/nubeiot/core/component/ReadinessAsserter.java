package com.nubeiot.core.component;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.component.ApplicationProbeHandler.ApplicationReadinessHandler;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.ErrorData;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ReadinessAsserter implements ApplicationReadinessHandler {

    @NonNull
    private final TestContext context;
    @NonNull
    private final Async async;
    @NonNull
    private final JsonObject expected;

    @Override
    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    public boolean success(RequestData requestData) {
        JsonHelper.assertJson(context, async, expected, requestData.body());
        return true;
    }

    @Override
    @EventContractor(action = EventAction.NOTIFY_ERROR, returnType = boolean.class)
    public boolean error(ErrorData error) {
        JsonHelper.assertJson(context, async, expected, error.toJson());
        return true;
    }

}
