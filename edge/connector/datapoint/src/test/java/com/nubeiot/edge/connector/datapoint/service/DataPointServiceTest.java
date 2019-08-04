package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import org.junit.BeforeClass;

import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.sql.BaseSqlTest;
import com.nubeiot.edge.connector.datapoint.DataPointEntityHandler;
import com.nubeiot.iotdata.model.DefaultCatalog;

import lombok.NonNull;

public class DataPointServiceTest extends BaseSqlTest {

    @BeforeClass
    public static void beforeSuite() { BaseSqlTest.beforeSuite(); }

    @Override
    public void before(TestContext context) {
        super.before(context);
        DataPointEntityHandler entityHandler = startSQL(context, DefaultCatalog.DEFAULT_CATALOG,
                                                        DataPointEntityHandler.class);
        EventController controller = controller();
        DataPointService.createServices(entityHandler)
                        .forEach(service -> controller.register(service.address(), service));
    }

    @Override
    public void after(TestContext context) {
        super.after(context);
    }

    @Override
    @NonNull
    public String getJdbcUrl() {
        return "jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString();
    }

}
