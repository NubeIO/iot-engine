package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.sql.BaseSqlServiceTest;
import com.nubeiot.core.sql.BaseSqlTest;
import com.nubeiot.core.sql.JsonPojo;
import com.nubeiot.edge.connector.datapoint.DataPointConfig.BuiltinData;
import com.nubeiot.edge.connector.datapoint.MockDataPointEntityHandler;
import com.nubeiot.iotdata.model.DefaultCatalog;
import com.nubeiot.iotdata.model.tables.pojos.Device;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class DataPointServiceTest extends BaseSqlServiceTest {

    private static final Device DEVICE = new Device().setId(UUID.fromString("d7cd3f57-a188-4462-b959-df7a23994c92"))
                                                     .setCode("NUBEIO-0001")
                                                     .setCustomerCode("NUBEIO")
                                                     .setSiteCode("xxx")
                                                     .setPolicyId("yyy")
                                                     .setDataVersion("0.0.1");

    @BeforeClass
    public static void beforeSuite() { BaseSqlTest.beforeSuite(); }

    protected void setup(TestContext context) {
        SharedDataDelegate.addLocalDataValue(vertx, sharedKey, MockDataPointEntityHandler.BUILTIN_DATA,
                                             BuiltinData.def().toJson().put("device", DEVICE.toJson()));
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
        JsonObject expected = JsonPojo.from(DEVICE).toJson();
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("device_id", "d7cd3f57-a188-4462-b959-df7a23994c92"))
                                     .build();
        asserter(context, true, expected, DeviceService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_list_measureUnit(TestContext context) {
        JsonObject expected = new JsonObject("{\"units\":[{\"type\":\"number\",\"category\":\"ALL\"}," +
                                             "{\"type\":\"percentage\",\"category\":\"ALL\",\"symbol\":\"%\"}," +
                                             "{\"type\":\"voltage\",\"category\":\"ALL\",\"symbol\":\"V\"}," +
                                             "{\"type\":\"celsius\",\"category\":\"ALL\",\"symbol\":\"U+2103\"}," +
                                             "{\"type\":\"bool\",\"category\":\"ALL\",\"possible_values\":{\"0" +
                                             ".5\":[\"true\",\"on\",\"start\",\"1\"],\"0.0\":[\"false\",\"off\"," +
                                             "\"stop\",\"0\",\"null\"]}},{\"type\":\"dBm\",\"category\":\"ALL\"," +
                                             "\"symbol\":\"dBm\"},{\"type\":\"hPa\",\"category\":\"ALL\"," +
                                             "\"symbol\":\"hPa\"},{\"type\":\"lux\",\"category\":\"ALL\"," +
                                             "\"symbol\":\"lx\"},{\"type\":\"kWh\",\"category\":\"ALL\"," +
                                             "\"symbol\":\"kWh\"}]}");
        asserter(context, true, expected, MeasureUnitService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build());
    }

}
