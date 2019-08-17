package com.nubeiot.edge.module.scheduler;

import java.util.UUID;

import org.junit.BeforeClass;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.ExpectedResponse;
import com.nubeiot.core.http.dynamic.DynamicServiceTestBase;
import com.nubeiot.core.sql.SqlConfig;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public abstract class EdgeSchedulerVerticleTest extends DynamicServiceTestBase {

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("org.jooq")).setLevel(Level.DEBUG);
    }

    @Override
    protected DeploymentOptions getServiceOptions() {
        JsonObject sqlConfig = new JsonObject(
            "{\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString() + "\"}}");
        final JsonObject appConfig = new JsonObject().put(SqlConfig.NAME, sqlConfig);
        return new DeploymentOptions().setConfig(new JsonObject().put(AppConfig.NAME, appConfig));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected EdgeSchedulerVerticle service() {
        return new EdgeSchedulerVerticle(MockSchedulerEntityHandler.class);
    }

    void createJob(TestContext context, JobEntity job, ExpectedResponse expected) {
        final RequestData reqData = RequestData.builder().body(job.toJson()).build();
        assertRestByClient(context, HttpMethod.POST, "/api/s/job", reqData, expected);
    }

}
