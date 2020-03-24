package com.nubeiot.edge.bios;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.bios.loader.DeploymentAsserter;
import com.nubeiot.edge.installer.InstallerVerticle;
import com.nubeiot.edge.installer.loader.VertxModuleType;
import com.nubeiot.edge.installer.model.tables.pojos.Application;

@Ignore
public class ServiceNameDuplicationTest extends BaseInstallerVerticleTest {

    @Before
    public void before(TestContext context) {
        super.before(context);
        this.insertModule(context, new Application().setAppId(MODULE_ID)
                                                    .setServiceType(VertxModuleType.JAVA)
                                                    .setServiceName(SERVICE_NAME)
                                                    .setState(State.ENABLED)
                                                    .setVersion(VERSION)
                                                    .setSystemConfig(APP_SYSTEM_CONFIG)
                                                    .setAppConfig(APP_CONFIG)
                                                    .setModifiedAt(DateTimes.now()));
    }

    @Override
    protected InstallerVerticle initMockupVerticle(TestContext context) {
        return new MockBiosEdgeVerticle(DeploymentAsserter.init(vertx, context));
    }

    @Test
    public void test_create_with_service_name_duplicated(TestContext context) {
        JsonObject appConfig = new JsonObject().put("", "");
        JsonObject metadata = new JsonObject().put("state", State.ENABLED)
                                              .put("version", VERSION)
                                              .put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID + "test")
                                          .put("metadata", metadata)
                                          .put("appConfig", appConfig);

        executeThenAssert(EventAction.CREATE, context, body, response -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
        });
    }

}
