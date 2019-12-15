package com.nubeiot.edge.module.datapoint.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class MigrateCacheTest extends BaseDataPointServiceTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Override
    protected JsonObject testData() {
        return MockData.data_Device_Network();
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:h2:file:" + folder.getRoot().toPath().resolve("datapoint-cache").toString();
    }

    @Test
    public void test_restart_app(TestContext context) {
        assertCache(context);
        stopSQL(context);
        setup(context);
        assertCache(context);
    }

    private void assertCache(TestContext context) {
        context.assertEquals(PrimaryKey.DEVICE.toString(),
                             SharedDataDelegate.getLocalDataValue(vertx, sharedKey, DataPointIndex.DEVICE_ID));
        context.assertEquals(MockData.DEVICE.getCustomerCode(),
                             SharedDataDelegate.getLocalDataValue(vertx, sharedKey, DataPointIndex.CUSTOMER_CODE));
        context.assertEquals(MockData.DEVICE.getSiteCode(),
                             SharedDataDelegate.getLocalDataValue(vertx, sharedKey, DataPointIndex.SITE_CODE));
    }

}
