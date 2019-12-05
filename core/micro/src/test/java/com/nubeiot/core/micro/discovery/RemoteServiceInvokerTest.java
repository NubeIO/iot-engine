package com.nubeiot.core.micro.discovery;

import org.junit.Before;
import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.micro.BaseMicroServiceTest;
import com.nubeiot.core.micro.discovery.mock.MockServiceInvoker;
import com.nubeiot.core.micro.discovery.mock.MockServiceListener;

public class RemoteServiceInvokerTest extends BaseMicroServiceTest {

    @Before
    public void setUp(TestContext context) {
        super.setUp(context);
        eventClient.register(EVENT_ADDRESS_1, new MockServiceListener());
    }

    @Test
    public void test_get_not_found_service(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(config.getGatewayConfig().getIndexAddress(), eventClient,
                                                            EVENT_RECORD_1 + "...");
        invoker.execute(EventAction.CREATE, RequestData.builder().build())
               .subscribe(d -> TestHelper.testComplete(async), t -> {
                   context.assertTrue(t instanceof NubeException);
                   assert t instanceof NubeException;
                   NubeException e = (NubeException) t;
                   context.assertEquals(ErrorCode.SERVICE_NOT_FOUND, e.getErrorCode());
                   context.assertEquals(invoker.serviceLabel() +
                                        " is not found or out of service. Try again later | Error: SERVICE_NOT_FOUND",
                                        e.getMessage());
                   TestHelper.testComplete(async);
               });
    }

    @Test
    public void test_get_not_found_action(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(config.getGatewayConfig().getIndexAddress(), eventClient,
                                                            EVENT_RECORD_1);
        invoker.execute(EventAction.UNKNOWN, RequestData.builder().build())
               .subscribe(d -> TestHelper.testComplete(async), t -> {
                   context.assertTrue(t instanceof ServiceException);
                   assert t instanceof ServiceException;
                   ServiceException e = (ServiceException) t;
                   context.assertEquals(ErrorCode.SERVICE_ERROR, e.getErrorCode());
                   context.assertEquals(
                       "Unsupported '" + EventAction.UNKNOWN + "' at destination '" + EVENT_RECORD_1 + "' under '" +
                       invoker.serviceLabel() + "'", e.getMessage());
                   TestHelper.testComplete(async);
               });
    }

    @Test
    public void test_execute_service_failed(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(config.getGatewayConfig().getIndexAddress(), eventClient,
                                                            EVENT_RECORD_1);
        invoker.execute(EventAction.UPDATE, RequestData.builder().build())
               .subscribe((d, t) -> JsonHelper.assertJson(context, async,
                                                          new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                                          .put("message", "hey"), d));
    }

    @Test
    public void test_execute_service_success(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(config.getGatewayConfig().getIndexAddress(), eventClient,
                                                            EVENT_RECORD_1);
        final JsonObject expected = new JsonObject().put(Headers.X_REQUEST_BY, invoker.requester())
                                                    .put("action", EventAction.CREATE);
        invoker.execute(EventAction.CREATE, RequestData.builder().build())
               .subscribe(d -> JsonHelper.assertJson(context, async, expected, d));
    }

}
