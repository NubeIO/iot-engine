package com.nubeiot.edge.module.datapoint;

import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.sql.service.BaseSqlServiceTest;
import com.nubeiot.edge.module.datapoint.service.DataPointService;
import com.nubeiot.iotdata.edge.model.DefaultCatalog;

@RunWith(VertxUnitRunner.class)
public abstract class BaseDataPointServiceTest extends BaseSqlServiceTest {

    protected void setup(TestContext context) {
        SharedDataDelegate.addLocalDataValue(vertx, sharedKey, DataPointIndex.BUILTIN_DATA, testData());
        DataPointEntityHandler entityHandler = startSQL(context, DefaultCatalog.DEFAULT_CATALOG,
                                                        DataPointEntityHandler.class);
        EventController controller = controller();
        DataPointService.createServices(entityHandler)
                        .forEach(service -> controller.register(service.address(), service));
    }

    protected abstract JsonObject testData();

}
