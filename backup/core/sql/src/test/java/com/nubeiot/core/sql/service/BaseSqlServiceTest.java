package com.nubeiot.core.sql.service;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.sql.BaseSqlTest;

public abstract class BaseSqlServiceTest extends BaseSqlTest {

    protected void asserter(TestContext context, boolean isSuccess, JsonObject expected, String address,
                            EventAction action, RequestData reqData) {
        asserter(context, isSuccess, expected, address, action, reqData, JSONCompareMode.STRICT);
    }

    protected void asserter(TestContext context, boolean isSuccess, JsonObject expected, String address,
                            EventAction action, RequestData reqData, Customization... customizations) {
        asserter(context, isSuccess, expected, address, action, reqData, JSONCompareMode.STRICT, customizations);
    }

    protected void asserter(TestContext context, boolean isSuccess, JsonObject expected, String address,
                            EventAction action, RequestData reqData, JSONCompareMode mode,
                            Customization... customizations) {
        asserter(context, isSuccess, expected, address, action, reqData, new CountDownLatch(1), mode, customizations);
    }

    void asserter(TestContext context, boolean isSuccess, JsonObject expected, String address, EventAction action,
                  RequestData reqData, CountDownLatch latch, JSONCompareMode mode, Customization... customizations) {
        final Async async = context.async();
        controller().request(address, EventMessage.initial(action, reqData),
                             ReplyEventHandler.builder().address(address).action(action).success(msg -> {
                                 latch.countDown();
                                 asserter(context, async, isSuccess, expected, msg, mode, customizations);
                             }).build());
    }

    private void asserter(TestContext context, Async async, boolean isSuccess, JsonObject expected, EventMessage msg,
                          JSONCompareMode mode, Customization... customizations) {
        System.out.println("RESPONSE:" + msg.toJson().encode());
        context.assertEquals(isSuccess, msg.isSuccess());
        if (customizations.length == 0) {
            JsonHelper.assertJson(context, async, expected,
                                  isSuccess ? Objects.requireNonNull(msg.getData()) : msg.getError().toJson(), mode);
        } else {
            JsonHelper.assertJson(context, async, expected,
                                  isSuccess ? Objects.requireNonNull(msg.getData()) : msg.getError().toJson(),
                                  customizations);
        }
    }

}
