package com.nubeiot.edge.connector.datapoint;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.dynamic.DynamicServiceTestBase;
import com.nubeiot.core.sql.JsonPojo;
import com.nubeiot.core.sql.SqlConfig;
import com.nubeiot.edge.connector.datapoint.DataPointConfig.BuiltinData;
import com.nubeiot.edge.connector.datapoint.service.DataPointServiceTest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(VertxUnitRunner.class)
public class DataPointGatewayVerticleTest extends DynamicServiceTestBase {

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.INFO);
    }

    @Override
    protected DeploymentOptions getServiceOptions() {
        BuiltinData def = BuiltinData.def();
        def.put("device", DataPointServiceTest.DEVICE.toJson());
        JsonObject sqlConfig = new JsonObject(
            "{\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString() + "\"}}");
        final JsonObject appConfig = new JsonObject().put(DataPointConfig.NAME, DataPointConfig.def(def).toJson())
                                                     .put(SqlConfig.NAME, sqlConfig);
        return new DeploymentOptions().setConfig(new JsonObject().put(AppConfig.NAME, appConfig));
    }

    @Override
    protected <T extends ContainerVerticle> T service() {
        return (T) new DataPointVerticle();
    }

    @Test
    public void test_event_not_found(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/device/" + DataPointServiceTest.DEVICE.getId(), 200,
                           JsonPojo.from(DataPointServiceTest.DEVICE).toJson().put("data_version", "0.0.1"));
    }

}
