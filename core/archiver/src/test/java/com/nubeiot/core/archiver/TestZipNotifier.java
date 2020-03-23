package com.nubeiot.core.archiver;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

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
    private final CountDownLatch latch;

    @Override
    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    public boolean success(@NonNull ZipOutput response) {
        try {
            System.out.println(response.toJson());
            JsonHelper.assertJson(testContext, async, expected, response.toJson(), JsonHelper.ignore("lastModified"));
        } finally {
            Optional.ofNullable(latch).ifPresent(CountDownLatch::countDown);
        }
        return true;
    }

    @Override
    @EventContractor(action = EventAction.NOTIFY_ERROR, returnType = boolean.class)
    public boolean error(@NonNull ErrorData error) {
        try {
            System.out.println(error.toJson());
            JsonHelper.assertJson(testContext, async, expected, error.getError().toJson());
        } finally {
            Optional.ofNullable(latch).ifPresent(CountDownLatch::countDown);
        }
        return true;
    }

}
