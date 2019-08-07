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
import com.nubeiot.edge.connector.datapoint.DataPointConfig.BuiltinData;
import com.nubeiot.edge.connector.datapoint.MockDataPointEntityHandler;
import com.nubeiot.iotdata.model.DefaultCatalog;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class DataPointServiceTest extends BaseSqlServiceTest {

    @BeforeClass
    public static void beforeSuite() { BaseSqlTest.beforeSuite(); }

    protected void setup(TestContext context) {
        MockDataPointEntityHandler entityHandler = startSQL(context, DefaultCatalog.DEFAULT_CATALOG,
                                                            MockDataPointEntityHandler.class);
        SharedDataDelegate.addLocalDataValue(vertx, sharedKey, MockDataPointEntityHandler.BUILTIN_DATA,
                                             new BuiltinData());
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
    public void testGetListMeasure(TestContext context) {
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
