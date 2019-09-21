package com.nubeiot.edge.module.datapoint;

import java.util.UUID;
import java.util.function.Function;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.Customization;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.http.dynamic.DynamicServiceTestBase;
import com.nubeiot.core.sql.SqlConfig;
import com.nubeiot.edge.module.datapoint.DataPointConfig.BuiltinData;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(VertxUnitRunner.class)
public abstract class BaseDataPointVerticleTest extends DynamicServiceTestBase {

    protected static final Function<String, Customization> IGNORE = JsonHelper::ignore;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("org.jooq")).setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("com.zaxxer.hikari")).setLevel(Level.INFO);
    }

    @Override
    protected final DeploymentOptions getServiceOptions() {
        JsonObject sqlConfig = new JsonObject(
            "{\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString() + "\"}}");
        final JsonObject appConfig = new JsonObject().put(DataPointConfig.NAME, dataPointConfig().toJson())
                                                     .put(SqlConfig.NAME, sqlConfig);
        return new DeploymentOptions().setConfig(new JsonObject().put(AppConfig.NAME, appConfig));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final DataPointVerticle service() {
        return new DataPointVerticle();
    }

    private DataPointConfig dataPointConfig() {
        return DataPointConfig.def(JsonData.from(builtinData(), BuiltinData.class), syncConfig());
    }

    protected abstract JsonObject builtinData();

    protected DataSyncConfig syncConfig() {
        return DataSyncConfig.def();
    }

}
