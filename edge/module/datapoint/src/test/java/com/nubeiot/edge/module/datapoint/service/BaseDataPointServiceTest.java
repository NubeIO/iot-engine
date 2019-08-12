package com.nubeiot.edge.module.datapoint.service;

import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.sql.BaseSqlServiceTest;
import com.nubeiot.edge.module.datapoint.MockDataPointEntityHandler;
import com.nubeiot.iotdata.model.DefaultCatalog;

@RunWith(VertxUnitRunner.class)
public abstract class BaseDataPointServiceTest extends BaseSqlServiceTest {

    protected void setup(TestContext context) {
        SharedDataDelegate.addLocalDataValue(vertx, sharedKey, MockDataPointEntityHandler.BUILTIN_DATA, testData());
        MockDataPointEntityHandler entityHandler = startSQL(context, DefaultCatalog.DEFAULT_CATALOG,
                                                            MockDataPointEntityHandler.class);
        EventController controller = controller();
        DataPointService.createServices(entityHandler)
                        .forEach(service -> controller.register(service.address(), service));
    }

    protected abstract JsonObject testData();

}
