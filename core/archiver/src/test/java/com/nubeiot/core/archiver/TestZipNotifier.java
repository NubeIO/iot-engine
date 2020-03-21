package com.nubeiot.core.archiver;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.ErrorData;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TestZipNotifier implements ZipNotificationHandler {

    @NonNull
    private final TestContext testContext;
    @NonNull
    private final Async async;
    @NonNull
    private final JsonObject expected;

    @Override
    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    public boolean success(ZipOutput response) {
        JsonHelper.assertJson(testContext, async, expected, response.toJson(), JsonHelper.ignore("lastModified"));
        return true;
    }

    @Override
    @EventContractor(action = EventAction.NOTIFY_ERROR, returnType = boolean.class)
    public boolean error(@NonNull ErrorData error) {
        JsonHelper.assertJson(testContext, async, expected, error.toJson());
        return true;
    }

}
