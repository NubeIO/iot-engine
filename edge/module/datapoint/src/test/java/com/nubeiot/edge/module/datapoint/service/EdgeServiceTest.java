package com.nubeiot.edge.module.datapoint.service;

import java.util.Objects;

import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.reactivex.Maybe;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.type.SyncAudit;
import com.nubeiot.core.sql.type.TimeAudit;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class EdgeServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Edge_Network();
    }

    @Test
    public void test_list_measureUnit(TestContext context) {
        asserter(context, true, MockData.MEASURE_UNITS, MeasureUnitService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void test_get_edge(TestContext context) {
        JsonObject userAgent = new JsonObject(
            "{\"__data_sync__\":{\"clientConfig\":{\"userAgent\":\"nubeio.edge.datapoint/1.0.0 " +
            UUID64.uuidToBase64(PrimaryKey.EDGE) + "\"}}}");
        JsonObject expected = JsonPojo.from(MockData.EDGE)
                                      .toJson()
                                      .put("model", "Nube EdgeIO-28")
                                      .put("firmware_version", "v2")
                                      .put("metadata", userAgent)
                                      .put("data_version", "0.0.2");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("edge_id", UUID64.uuidToBase64(PrimaryKey.EDGE)))
                                     .build();
        asserter(context, true, expected, EdgeService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_edge_with_audit(TestContext context) {
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("edge_id", PrimaryKey.EDGE.toString()))
                                     .filter(new JsonObject().put(Filters.AUDIT, true))
                                     .build();
        DeliveryEvent event = DeliveryEvent.builder()
                                           .action(EventAction.GET_ONE)
                                           .address(EdgeService.class.getName())
                                           .addPayload(req)
                                           .build();
        controller().fire(event, EventbusHelper.replyAsserter(context, registerAsserter(context)));
    }

    @Test
    @Ignore
    public void test_create_measure_unit(TestContext context) {
        final JsonObject data = new JsonObject(
            "{\"type\":\"millimeters_per_second\",\"symbol\":\"mm/s\",\"category\":\"VELOCITY\"}");
        final RequestData req = RequestData.builder().body(data).build();
        final RequestData reqGet = RequestData.builder()
                                              .body(new JsonObject().put("unit_type", "millimeters_per_second"))
                                              .build();
        Async async = context.async();
        controller().request(MeasureUnitService.class.getName(), EventMessage.initial(EventAction.CREATE, req))
                    .doOnSuccess(msg -> System.out.println(msg.toJson()))
                    .flatMap(msg -> controller().request(MeasureUnitService.class.getName(),
                                                         EventMessage.initial(EventAction.GET_ONE, reqGet)))
                    .filter(EventMessage::isError)
                    .map(EventMessage::getError)
                    .map(error -> {
                        System.out.println("Has error");
                        JsonHelper.assertJson(context, async, new JsonObject(
                            "{\"code\":\"NOT_FOUND\"," + "\"message\":\"Not found resource with " +
                            "unit_type=millimeters_per_second\"}"), error.toJson());
                        return error.toJson();
                    })
                    .switchIfEmpty(Maybe.error(new RuntimeException("Should not here")))
                    .subscribe(s -> TestHelper.testComplete(async), context::fail);
    }

    private Handler<JsonObject> registerAsserter(TestContext context) {
        return body -> {
            final JsonObject data = Objects.requireNonNull(body.getJsonObject("data"));
            final TimeAudit timeAudit = JsonData.from(data.getJsonObject("time_audit"), TimeAudit.class);
            final SyncAudit syncAudit = JsonData.from(data.getJsonObject("sync_audit"), SyncAudit.class);
            context.assertNotNull(timeAudit.getCreatedTime());
            context.assertEquals("SYSTEM_INITIATOR", timeAudit.getCreatedBy());
            context.assertNull(timeAudit.getLastModifiedTime());
            context.assertNull(timeAudit.getLastModifiedBy());
            context.assertEquals(1, timeAudit.getRevision());
            context.assertEquals(Status.INITIAL, syncAudit.getStatus());
            context.assertNull(syncAudit.getSyncedTime());
        };
    }

}
