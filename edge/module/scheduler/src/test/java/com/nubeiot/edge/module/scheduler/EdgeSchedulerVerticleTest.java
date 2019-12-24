package com.nubeiot.edge.module.scheduler;

import java.util.UUID;
import java.util.function.Function;

import org.junit.BeforeClass;
import org.skyscreamer.jsonassert.Customization;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.component.ReadinessAsserter;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.http.ExpectedResponse;
import com.nubeiot.core.http.dynamic.DynamicServiceTestBase;
import com.nubeiot.core.sql.SqlConfig;
import com.nubeiot.edge.module.scheduler.MockSchedulerEntityHandler.MockSchedulerSchemaHandler;
import com.nubeiot.scheduler.SchedulerConfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public abstract class EdgeSchedulerVerticleTest extends DynamicServiceTestBase {

    static final Function<String, Customization> UTC_DATE = timeKey -> JsonHelper.ignore(timeKey + ".utc");
    static final Function<String, Customization> LOCAL_DATE = timeKey -> JsonHelper.ignore(timeKey + ".local");

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("org.jooq")).setLevel(Level.DEBUG);
    }

    @Override
    protected void startGatewayAndService(TestContext context, ContainerVerticle service,
                                          DeploymentOptions serviceOptions) {
        final EventbusClient client = SharedDataDelegate.getEventController(vertx.getDelegate(),
                                                                            service.getClass().getName());
        client.register(MockSchedulerSchemaHandler.class.getName() + ".readiness",
                        new ReadinessAsserter(context, context.async(), new JsonObject("{\"records\":7}")));
        super.startGatewayAndService(context, service, serviceOptions);
        TestHelper.sleep(500);
    }

    @Override
    protected DeploymentOptions getServiceOptions() {
        JsonObject sqlConfig = new JsonObject(
            "{\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString() + "\"}}");
        JsonObject appConfig = new JsonObject().put(SqlConfig.NAME, sqlConfig)
                                               .put(SchedulerConfig.NAME, new JsonObject().put("schedulerName",
                                                                                               UUID.randomUUID()
                                                                                                   .toString()));
        return new DeploymentOptions().setConfig(new JsonObject().put(AppConfig.NAME, appConfig));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected EdgeSchedulerVerticle service() {
        return new EdgeSchedulerVerticle(MockSchedulerEntityHandler.class);
    }

    @Override
    protected int timeoutInSecond() {
        return super.timeoutInSecond() * 2;
    }

    void createJob(TestContext context, JsonObject job, ExpectedResponse expected) {
        final RequestData reqData = RequestData.builder().body(job).build();
        assertRestByClient(context, HttpMethod.POST, "/api/s/job", reqData, expected);
    }

}
