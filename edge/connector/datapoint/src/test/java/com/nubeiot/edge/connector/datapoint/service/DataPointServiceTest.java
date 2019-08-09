package com.nubeiot.edge.connector.datapoint.service;

import java.util.Objects;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.sql.BaseSqlServiceTest;
import com.nubeiot.core.sql.BaseSqlTest;
import com.nubeiot.core.sql.JsonPojo;
import com.nubeiot.core.sql.type.SyncAudit;
import com.nubeiot.core.sql.type.TimeAudit;
import com.nubeiot.edge.connector.datapoint.MockData;
import com.nubeiot.edge.connector.datapoint.MockData.PrimaryKey;
import com.nubeiot.edge.connector.datapoint.MockDataPointEntityHandler;
import com.nubeiot.iotdata.model.DefaultCatalog;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class DataPointServiceTest extends BaseSqlServiceTest {

    @BeforeClass
    public static void beforeSuite() { BaseSqlTest.beforeSuite(); }

    protected void setup(TestContext context) {
        SharedDataDelegate.addLocalDataValue(vertx, sharedKey, MockDataPointEntityHandler.BUILTIN_DATA,
                                             MockData.fullData());
        MockDataPointEntityHandler entityHandler = startSQL(context, DefaultCatalog.DEFAULT_CATALOG,
                                                            MockDataPointEntityHandler.class);
        EventController controller = controller();
        DataPointService.createServices(entityHandler)
                        .forEach(service -> controller.register(service.address(), service));
    }

    @Override
    @NonNull
    public String getJdbcUrl() {
        return "jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString();
    }

    @Test
    public void test_get_device(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.DEVICE).toJson();
        RequestData req = RequestData.builder().body(new JsonObject().put("device_id", PrimaryKey.DEVICE.toString()))
                                     .build();
        asserter(context, true, expected, DeviceService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_device_with_audit(TestContext context) {
        RequestData req = RequestData.builder().body(new JsonObject().put("device_id", PrimaryKey.DEVICE.toString()))
                                     .filter(new JsonObject().put(Filters.AUDIT, true))
                                     .build();
        final Async async = context.async();
        final ReplyEventHandler replyAsserter = ReplyEventHandler.builder().action(EventAction.GET_ONE).success(msg -> {
            System.out.println(msg.toJson().encode());
            try {
                final JsonObject data = Objects.requireNonNull(msg.getData());
                final TimeAudit timeAudit = JsonData.from(data.getJsonObject("time_audit"), TimeAudit.class);
                final SyncAudit syncAudit = JsonData.from(data.getJsonObject("sync_audit"), SyncAudit.class);
                context.assertNotNull(timeAudit.getCreatedTime());
                context.assertEquals("SYSTEM_INITIATOR", timeAudit.getCreatedBy());
                context.assertNull(timeAudit.getLastModifiedTime());
                context.assertNull(timeAudit.getLastModifiedBy());
                context.assertFalse(syncAudit.isSynced());
                context.assertNull(syncAudit.getSyncedTime());
            } finally {
                TestHelper.testComplete(async);
            }
        }).build();
        controller().request(DeviceService.class.getName(), EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_ONE, req), replyAsserter);
    }

    @Test
    public void test_list_measureUnit(TestContext context) {
        asserter(context, true, MockData.MEASURE_UNITS, MeasureUnitService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build());
    }

    @Test
    public void test_list_network(TestContext context) {
        JsonObject expected = new JsonObject().put("networks",
                                                   new JsonArray().add(JsonPojo.from(MockData.NETWORK).toJson()));
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build());
    }

    @Test
    public void test_network_by_device(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.NETWORK).toJson();
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", PrimaryKey.DEVICE.toString())
                                                           .put("network_id", PrimaryKey.NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_list_point(TestContext context) {

    }

}
